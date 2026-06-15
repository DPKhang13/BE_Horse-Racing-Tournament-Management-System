package com.group5.htms.service.impl;

import com.group5.htms.dto.payment.request.VnpayCreatePaymentRequest;
import com.group5.htms.dto.payment.response.PaymentTransactionResponse;
import com.group5.htms.dto.payment.response.VnpayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.VnpayReturnResponse;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.PaymentGatewayProvider;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.VnpayResponseCodeStatus;
import com.group5.htms.enums.WalletStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.UnauthorizedException;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.VnpayPaymentService;
import com.group5.htms.util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import com.group5.htms.config.VnpayConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnpayPaymentServiceImpl implements VnpayPaymentService {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE = BigDecimal.ONE;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter VNPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final VnpayConfig vnpayConfig;
    private final UsersRepository usersRepository;
    private final WalletsRepository walletsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;

    /*
     * POST /api/payments/vnpay/create-payment
     *
     * Flow:
     * 1. Lấy user đang login.
     * 2. Check user có role spectator active.
     * 3. Lấy hoặc tạo wallet của spectator.
     * 4. Tạo WalletTransaction status = pending.
     * 5. Generate gateway_txn_ref random.
     * 6. Tạo VNPay URL với vnp_TxnRef = gateway_txn_ref.
     */
    @Override
    @Transactional
    public VnpayCreatePaymentResponse createPaymentUrl(
            VnpayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Users currentUser = getCurrentUser();

        validateSpectator(currentUser);

        Wallets wallet = getOrCreateWallet(currentUser);

        BigDecimal cashAmount = normalizeAmount(request.getAmount());

        BigDecimal pointsAmount = cashAmount.multiply(DEFAULT_EXCHANGE_RATE);

        BigDecimal currentBalance = safeMoney(wallet.getPointBalance());

        WalletTransactions transaction = WalletTransactions.builder()
                .wallets(wallet)
                .users(currentUser)
                .txType(WalletTransactionType.TOPUP.getValue())
                .cashAmount(cashAmount)
                .pointsAmount(pointsAmount)
                .exchangeRate(DEFAULT_EXCHANGE_RATE)
                .pointsBefore(currentBalance)
                .pointsAfter(currentBalance)
                .status(WalletTransactionStatus.PENDING.getValue())
                .refType(PaymentGatewayProvider.VNPAY.getValue())
                .gatewayProvider(PaymentGatewayProvider.VNPAY.getValue())
                .createdBy(currentUser)
                .createdAt(Instant.now())
                .build();

        WalletTransactions savedTransaction =
                walletTransactionsRepository.save(transaction);

        String gatewayTxnRef = generateGatewayTxnRef(savedTransaction.getId());

        savedTransaction.setGatewayTxnRef(gatewayTxnRef);

        walletTransactionsRepository.save(savedTransaction);

        String paymentUrl = buildVnpayPaymentUrl(
                request,
                httpServletRequest,
                currentUser,
                cashAmount,
                gatewayTxnRef
        );

        return VnpayCreatePaymentResponse.builder()
                .txnRef(gatewayTxnRef)
                .transactionRef(gatewayTxnRef)
                .paymentUrl(paymentUrl)
                .transaction(toPaymentTransactionResponse(savedTransaction))
                .build();
    }

    /*
     * Return URL:
     * - Chỉ dùng để hiển thị kết quả thanh toán cho frontend.
     * - Không cộng point ở đây.
     * - Không update Wallet ở đây.
     */
    @Override
    public VnpayReturnResponse handleReturn(Map<String, String[]> parameterMap) {
        boolean validSignature = VnpayUtil.verifySignature(
                parameterMap,
                vnpayConfig.getHashSecret()
        );

        String responseCode =
                VnpayUtil.getFirstValue(parameterMap, "vnp_ResponseCode");

        String transactionStatus =
                VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionStatus");

        boolean success = validSignature
                && VnpayResponseCodeStatus.SUCCESS.getValue().equals(responseCode)
                && VnpayResponseCodeStatus.SUCCESS.getValue().equals(transactionStatus);

        String txnRef = VnpayUtil.getFirstValue(parameterMap, "vnp_TxnRef");
        WalletTransactions transaction = findTransactionByGatewayTxnRef(txnRef);

        return VnpayReturnResponse.builder()
                .validSignature(validSignature)
                .success(success)
                .txnRef(txnRef)
                .transactionRef(txnRef)
                .amount(VnpayUtil.getFirstValue(parameterMap, "vnp_Amount"))
                .responseCode(responseCode)
                .transactionStatus(transactionStatus)
                .transactionNo(VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionNo"))
                .bankCode(VnpayUtil.getFirstValue(parameterMap, "vnp_BankCode"))
                .payDate(VnpayUtil.getFirstValue(parameterMap, "vnp_PayDate"))
                .message(success ? "Payment success" : "Payment failed or invalid signature")
                .transaction(toPaymentTransactionResponse(transaction))
                .build();
    }

    /*
     * IPN:
     * Đây là nơi update DB thật.
     *
     * Flow:
     * 1. Verify secure hash.
     * 2. Lấy vnp_TxnRef.
     * 3. Lookup WalletTransaction bằng gatewayProvider + gatewayTxnRef.
     * 4. Lock transaction row.
     * 5. Check amount.
     * 6. Check status pending.
     * 7. Nếu success: cộng point vào Wallet + set completed.
     * 8. Nếu failed: set failed, không cộng point.
     */
    @Override
    @Transactional
    public Map<String, String> handleIpn(Map<String, String[]> parameterMap) {
        boolean validSignature = VnpayUtil.verifySignature(
                parameterMap,
                vnpayConfig.getHashSecret()
        );

        if (!validSignature) {
            return Map.of(
                    "RspCode", VnpayResponseCodeStatus.INVALID_SIGNATURE.getValue(),
                    "Message", "Invalid signature"
            );
        }

        String gatewayTxnRef =
                VnpayUtil.getFirstValue(parameterMap, "vnp_TxnRef");

        if (gatewayTxnRef == null || gatewayTxnRef.isBlank()) {
            return Map.of(
                    "RspCode", VnpayResponseCodeStatus.ORDER_NOT_FOUND.getValue(),
                    "Message", "Order not found"
            );
        }

        WalletTransactions transaction = walletTransactionsRepository
                .findFirstByGatewayProviderAndGatewayTxnRef(
                        PaymentGatewayProvider.VNPAY.getValue(),
                        gatewayTxnRef
                )
                .orElse(null);

        if (transaction == null) {
            return Map.of(
                    "RspCode", VnpayResponseCodeStatus.ORDER_NOT_FOUND.getValue(),
                    "Message", "Order not found"
            );
        }

        BigDecimal vnpayAmount = extractVnpayAmount(parameterMap);

        if (vnpayAmount == null
                || vnpayAmount.compareTo(transaction.getCashAmount()) != 0) {

            saveGatewayResponse(transaction, parameterMap);

            walletTransactionsRepository.save(transaction);

            return Map.of(
                    "RspCode", VnpayResponseCodeStatus.INVALID_AMOUNT.getValue(),
                    "Message", "Invalid Amount"
            );
        }

        /*
         * Idempotency protection:
         * Nếu transaction không còn pending thì nghĩa là đã xử lý rồi.
         * Không được cộng point lần 2.
         */
        if (!WalletTransactionStatus.PENDING.getValue()
                .equalsIgnoreCase(transaction.getStatus())) {

            return Map.of(
                    "RspCode", VnpayResponseCodeStatus.ORDER_ALREADY_CONFIRMED.getValue(),
                    "Message", "Order already confirmed"
            );
        }

        saveGatewayResponse(transaction, parameterMap);

        String responseCode =
                VnpayUtil.getFirstValue(parameterMap, "vnp_ResponseCode");

        String transactionStatus =
                VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionStatus");

        boolean paymentSuccess = VnpayResponseCodeStatus.SUCCESS.getValue().equals(responseCode)
                && VnpayResponseCodeStatus.SUCCESS.getValue().equals(transactionStatus);

        if (paymentSuccess) {
            completeTopUpTransaction(transaction);
        } else {
            failTopUpTransaction(transaction);
        }

        walletTransactionsRepository.save(transaction);

        /*
         * RspCode = 00 nghĩa là backend đã nhận và xử lý IPN.
         * Payment success hay failed được lưu bằng transaction.status trong DB.
         */
        return Map.of(
                "RspCode", VnpayResponseCodeStatus.SUCCESS.getValue(),
                "Message", "Confirm Success"
        );
    }

    private String buildVnpayPaymentUrl(
            VnpayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest,
            Users currentUser,
            BigDecimal cashAmount,
            String gatewayTxnRef
    ) {
        String clientIp = VnpayUtil.getClientIp(httpServletRequest);

        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        LocalDateTime expire = now.plusMinutes(15);

        String vnpAmount = cashAmount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toBigInteger()
                .toString();

        Map<String, String> params = new TreeMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayConfig.getTmnCode());
        params.put("vnp_Amount", vnpAmount);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", gatewayTxnRef);
        params.put(
                "vnp_OrderInfo",
                "Topup wallet for user " + currentUser.getUsername()
                        + " txn " + gatewayTxnRef
        );
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_Locale", normalizeLocale(request.getLocale()));
        params.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", expire.format(VNPAY_DATE_FORMAT));

        if (request.getBankCode() != null && !request.getBankCode().isBlank()) {
            params.put("vnp_BankCode", request.getBankCode().trim());
        }

        String hashData = VnpayUtil.buildHashData(params);
        String secureHash = VnpayUtil.hmacSHA512(
                vnpayConfig.getHashSecret(),
                hashData
        );

        return vnpayConfig.getPayUrl()
                + "?"
                + VnpayUtil.buildQueryString(params)
                + "&vnp_SecureHash="
                + secureHash;
    }

    private void completeTopUpTransaction(WalletTransactions transaction) {
        Wallets wallet = walletsRepository
                .findFirstById(transaction.getWallets().getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        BigDecimal pointsBefore = safeMoney(wallet.getPointBalance());

        BigDecimal pointsAfter =
                pointsBefore.add(transaction.getPointsAmount());

        wallet.setPointBalance(pointsAfter);

        transaction.setPointsBefore(pointsBefore);
        transaction.setPointsAfter(pointsAfter);
        transaction.setStatus(WalletTransactionStatus.COMPLETED.getValue());

        walletsRepository.save(wallet);
    }

    private void failTopUpTransaction(WalletTransactions transaction) {
        transaction.setStatus(WalletTransactionStatus.FAILED.getValue());
    }

    private void saveGatewayResponse(
            WalletTransactions transaction,
            Map<String, String[]> parameterMap
    ) {
        transaction.setGatewayTransactionNo(
                VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionNo")
        );

        transaction.setGatewayResponseCode(
                VnpayUtil.getFirstValue(parameterMap, "vnp_ResponseCode")
        );

        transaction.setGatewayTransactionStatus(
                VnpayUtil.getFirstValue(parameterMap, "vnp_TransactionStatus")
        );

        transaction.setGatewayBankCode(
                VnpayUtil.getFirstValue(parameterMap, "vnp_BankCode")
        );

        transaction.setGatewayPayDate(
                VnpayUtil.getFirstValue(parameterMap, "vnp_PayDate")
        );

        transaction.setGatewayRawResponse(buildRawResponse(parameterMap));
    }

    private BigDecimal extractVnpayAmount(Map<String, String[]> parameterMap) {
        String rawAmount =
                VnpayUtil.getFirstValue(parameterMap, "vnp_Amount");

        if (rawAmount == null || rawAmount.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(rawAmount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return null;
        }
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private void validateSpectator(Users user) {
        if (!RoleType.SPECTATOR.getValue().equalsIgnoreCase(user.getRoleType())) {
            throw new UnauthorizedException("Only spectator can top up wallet");
        }
    }

    private Wallets getOrCreateWallet(Users user) {
        return walletsRepository.findByUsersId(user.getId())
                .orElseGet(() -> {
                    Wallets wallet = Wallets.builder()
                            .users(user)
                            .pointBalance(BigDecimal.ZERO)
                            .status(WalletStatus.ACTIVE.getValue())
                            .createdAt(Instant.now())
                            .build();

                    return walletsRepository.save(wallet);
                });
    }

    private String generateGatewayTxnRef(Integer transactionId) {
        String randomSuffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "TOPUP-" + transactionId + "-" + randomSuffix;
    }

    private WalletTransactions findTransactionByGatewayTxnRef(String gatewayTxnRef) {
        if (gatewayTxnRef == null || gatewayTxnRef.isBlank()) {
            return null;
        }

        return walletTransactionsRepository
                .findByGatewayProviderAndGatewayTxnRef(
                        PaymentGatewayProvider.VNPAY.getValue(),
                        gatewayTxnRef
                )
                .orElse(null);
    }

    private PaymentTransactionResponse toPaymentTransactionResponse(
            WalletTransactions transaction
    ) {
        if (transaction == null) {
            return null;
        }

        return PaymentTransactionResponse.builder()
                .txId(transaction.getId())
                .walletId(transaction.getWallets() != null
                        ? transaction.getWallets().getId()
                        : null)
                .userId(transaction.getUsers() != null
                        ? transaction.getUsers().getId()
                        : null)
                .txType(transaction.getTxType())
                .cashAmount(transaction.getCashAmount())
                .pointsAmount(transaction.getPointsAmount())
                .exchangeRate(transaction.getExchangeRate())
                .pointsBefore(transaction.getPointsBefore())
                .pointsAfter(transaction.getPointsAfter())
                .status(transaction.getStatus())
                .refType(transaction.getRefType())
                .refId(transaction.getRefId())
                .createdBy(transaction.getCreatedBy() != null
                        ? transaction.getCreatedBy().getId()
                        : null)
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    private String normalizeLocale(String locale) {
        if ("en".equalsIgnoreCase(locale)) {
            return "en";
        }

        return "vn";
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Amount is required");
        }

        if (amount.compareTo(BigDecimal.valueOf(10000)) < 0) {
            throw new BadRequestException("Minimum top-up amount is 10,000 VND");
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /*
     * Lưu raw response để debug/audit.
     *
     * Không lưu vnp_SecureHash vào raw response.
     * Không expose field gatewayRawResponse ra API public.
     */
    private String buildRawResponse(Map<String, String[]> parameterMap) {
        return parameterMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> !"vnp_SecureHash".equals(entry.getKey()))
                .filter(entry -> !"vnp_SecureHashType".equals(entry.getKey()))
                .map(entry -> {
                    String value = entry.getValue() != null
                            && entry.getValue().length > 0
                            ? entry.getValue()[0]
                            : "";

                    return entry.getKey() + "=" + value;
                })
                .sorted()
                .collect(Collectors.joining("&"));
    }
}

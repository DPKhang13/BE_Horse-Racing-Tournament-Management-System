package com.group5.htms.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group5.htms.config.MomoConfig;
import com.group5.htms.config.ZalopayConfig;
import com.group5.htms.dto.payment.request.PaymentGatewayCreatePaymentRequest;
import com.group5.htms.dto.payment.response.PaymentGatewayCreatePaymentResponse;
import com.group5.htms.dto.payment.response.PaymentGatewayReturnResponse;
import com.group5.htms.dto.payment.response.PaymentTransactionResponse;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.PaymentGatewayProvider;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.WalletStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.UnauthorizedException;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.PaymentGatewayService;
import com.group5.htms.util.PaymentGatewayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final BigDecimal DEFAULT_EXCHANGE_RATE = new BigDecimal("0.001");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter ZALOPAY_TRANS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final MomoConfig momoConfig;
    private final ZalopayConfig zalopayConfig;
    private final UsersRepository usersRepository;
    private final WalletsRepository walletsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;

    @Override
    @Transactional
    public PaymentGatewayCreatePaymentResponse createMomoPayment(
            PaymentGatewayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        validateMomoConfig();
        Users user = getCurrentUser();
        validateSpectator(user);
        BigDecimal cashAmount = normalizeAmount(request.getAmount());
        WalletTransactions tx = createPendingTopUp(getOrCreateWallet(user), user, cashAmount, PaymentGatewayProvider.MOMO);
        String txnRef = transactionRef(tx.getId());
        String amount = gatewayAmount(cashAmount);
        String extraData = "";
        String orderInfo = "Topup wallet txn " + txnRef;
        String requestType = blankToDefault(momoConfig.getRequestType(), "captureWallet");
        String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + momoConfig.getIpnUrl()
                + "&orderId=" + txnRef
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoConfig.getPartnerCode()
                + "&redirectUrl=" + momoConfig.getRedirectUrl()
                + "&requestId=" + txnRef
                + "&requestType=" + requestType;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", momoConfig.getPartnerCode());
        payload.put("partnerName", "HTMS");
        payload.put("storeId", "HTMS");
        payload.put("requestId", txnRef);
        payload.put("amount", amount);
        payload.put("orderId", txnRef);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", momoConfig.getRedirectUrl());
        payload.put("ipnUrl", momoConfig.getIpnUrl());
        payload.put("lang", "en".equalsIgnoreCase(request.getLocale()) ? "en" : "vi");
        payload.put("requestType", requestType);
        payload.put("autoCapture", true);
        payload.put("extraData", extraData);
        payload.put("signature", PaymentGatewayUtil.hmacSHA256(momoConfig.getSecretKey(), rawSignature));

        Map<String, Object> response = postJson(momoConfig.getCreateUrl(), payload);
        Integer resultCode = intValue(response.get("resultCode"));
        if (resultCode == null || resultCode != 0) {
            markFailed(tx);
            throw new BadRequestException("MoMo payment creation failed: " + response.getOrDefault("message", "unknown error"));
        }
        return PaymentGatewayCreatePaymentResponse.builder()
                .gateway(PaymentGatewayProvider.MOMO.getValue())
                .txnRef(txnRef)
                .transactionRef(txnRef)
                .paymentUrl(text(response.get("payUrl")))
                .deeplink(text(response.get("deeplink")))
                .qrCodeUrl(text(response.get("qrCodeUrl")))
                .gatewayOrderId(text(response.get("orderId")))
                .gatewayMessage(text(response.get("message")))
                .gatewayResultCode(resultCode)
                .transaction(toResponse(tx))
                .build();
    }

    @Override
    @Transactional
    public PaymentGatewayCreatePaymentResponse createZalopayPayment(
            PaymentGatewayCreatePaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        validateZalopayConfig();
        Users user = getCurrentUser();
        validateSpectator(user);
        BigDecimal cashAmount = normalizeAmount(request.getAmount());
        WalletTransactions tx = createPendingTopUp(getOrCreateWallet(user), user, cashAmount, PaymentGatewayProvider.ZALOPAY);
        String txnRef = transactionRef(tx.getId());
        String appTransId = LocalDateTime.now(VIETNAM_ZONE).format(ZALOPAY_TRANS_DATE_FORMAT) + "_" + txnRef;
        String appUser = user.getUsername() == null ? String.valueOf(user.getId()) : user.getUsername();
        String amount = gatewayAmount(cashAmount);
        String appTime = String.valueOf(System.currentTimeMillis());
        String item = "[]";
        String embedData = toJson(Map.of("redirecturl", zalopayConfig.getRedirectUrl(), "transactionRef", txnRef));
        String macData = zalopayConfig.getAppId() + "|" + appTransId + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + item;

        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", zalopayConfig.getAppId());
        form.put("app_user", appUser);
        form.put("app_time", appTime);
        form.put("amount", amount);
        form.put("app_trans_id", appTransId);
        form.put("embed_data", embedData);
        form.put("item", item);
        form.put("description", "Topup wallet txn " + txnRef);
        form.put("callback_url", zalopayConfig.getCallbackUrl());
        form.put("mac", PaymentGatewayUtil.hmacSHA256(zalopayConfig.getKey1(), macData));

        Map<String, Object> response = postForm(zalopayConfig.getCreateUrl(), form);
        Integer returnCode = intValue(response.get("return_code"));
        if (returnCode == null || returnCode != 1) {
            markFailed(tx);
            throw new BadRequestException("ZaloPay payment creation failed: " + response.getOrDefault("return_message", "unknown error"));
        }
        return PaymentGatewayCreatePaymentResponse.builder()
                .gateway(PaymentGatewayProvider.ZALOPAY.getValue())
                .txnRef(txnRef)
                .transactionRef(txnRef)
                .paymentUrl(text(response.get("order_url")))
                .deeplink(text(response.get("order_url")))
                .gatewayOrderId(appTransId)
                .gatewayMessage(text(response.get("return_message")))
                .gatewayResultCode(returnCode)
                .transaction(toResponse(tx))
                .build();
    }

    @Override
    @Transactional
    public PaymentGatewayReturnResponse handleMomoReturn(Map<String, String[]> parameterMap) {
        Map<String, Object> payload = firstValues(parameterMap);
        return processMomoPayload(payload, true);
    }

    @Override
    @Transactional
    public Map<String, Object> handleMomoIpn(Map<String, Object> payload) {
        PaymentGatewayReturnResponse result = processMomoPayload(payload, false);
        return Map.of("resultCode", result.isValidSignature() ? 0 : 1, "message", result.getMessage());
    }

    @Override
    @Transactional
    public PaymentGatewayReturnResponse handleZalopayReturn(Map<String, String[]> parameterMap) {
        String appTransId = firstNonBlank(PaymentGatewayUtil.firstValue(parameterMap, "apptransid"), PaymentGatewayUtil.firstValue(parameterMap, "app_trans_id"));
        String txnRef = extractZalopayTxnRef(appTransId);
        WalletTransactions tx = findTxForUpdate(txnRef);
        Map<String, Object> queryResponse = Map.of();
        boolean validQuery = false;
        String responseCode = PaymentGatewayUtil.firstValue(parameterMap, "status");
        String message = "Payment return received; waiting for ZaloPay confirmation";

        if (tx != null && appTransId != null && !appTransId.isBlank()) {
            try {
                queryResponse = queryZalopayOrder(appTransId);
                Integer returnCode = intValue(queryResponse.get("return_code"));
                responseCode = firstNonBlank(text(queryResponse.get("return_code")), responseCode);
                validQuery = returnCode != null;

                if (returnCode != null && returnCode == 1) {
                    BigDecimal confirmedAmount = decimal(queryResponse.get("amount"));
                    message = processTopUp(
                            tx,
                            PaymentGatewayProvider.ZALOPAY,
                            confirmedAmount == null ? tx.getCashAmount() : confirmedAmount,
                            true
                    );
                } else if (returnCode != null && returnCode == 2) {
                    message = processTopUp(tx, PaymentGatewayProvider.ZALOPAY, tx.getCashAmount(), false);
                } else {
                    message = firstNonBlank(text(queryResponse.get("return_message")), message);
                }
            } catch (BadRequestException ex) {
                message = "Cannot confirm ZaloPay payment: " + ex.getMessage();
            }
        }

        boolean success = tx != null && WalletTransactionStatus.COMPLETED.getValue().equalsIgnoreCase(tx.getStatus());
        return PaymentGatewayReturnResponse.builder()
                .gateway(PaymentGatewayProvider.ZALOPAY.getValue())
                .validSignature(validQuery)
                .success(success)
                .txnRef(txnRef)
                .transactionRef(txnRef)
                .amount(text(queryResponse.get("amount")))
                .responseCode(responseCode)
                .transactionStatus(tx == null ? null : tx.getStatus())
                .transactionNo(text(queryResponse.get("zp_trans_id")))
                .message(success ? "Payment success" : message)
                .transaction(toResponse(tx))
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> handleZalopayCallback(Map<String, Object> payload) {
        String data = text(payload.get("data"));
        String mac = text(payload.get("mac"));
        if (data == null || mac == null || !PaymentGatewayUtil.hmacSHA256(zalopayConfig.getKey2(), data).equalsIgnoreCase(mac)) {
            return Map.of("return_code", -1, "return_message", "Invalid mac");
        }
        Map<String, Object> dataPayload = readJson(data);
        String txnRef = extractZalopayTxnRef(text(dataPayload.get("app_trans_id")));
        WalletTransactions tx = findTxForUpdate(txnRef);
        String message = processTopUp(tx, PaymentGatewayProvider.ZALOPAY, decimal(dataPayload.get("amount")), true);
        boolean ok = "Payment success".equals(message) || "Payment already confirmed".equals(message);
        return Map.of("return_code", ok ? 1 : 0, "return_message", ok ? "success" : message);
    }

    private Map<String, Object> queryMomoOrder(String txnRef) {
        String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                + "&orderId=" + txnRef
                + "&partnerCode=" + momoConfig.getPartnerCode()
                + "&requestId=" + txnRef;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", momoConfig.getPartnerCode());
        payload.put("requestId", txnRef);
        payload.put("orderId", txnRef);
        payload.put("lang", "vi");
        payload.put("signature", PaymentGatewayUtil.hmacSHA256(momoConfig.getSecretKey(), rawSignature));
        return postJson(momoConfig.getQueryUrl(), payload);
    }

    private Map<String, Object> queryZalopayOrder(String appTransId) {
        String macData = zalopayConfig.getAppId() + "|" + appTransId + "|" + zalopayConfig.getKey1();
        Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", zalopayConfig.getAppId());
        form.put("app_trans_id", appTransId);
        form.put("mac", PaymentGatewayUtil.hmacSHA256(zalopayConfig.getKey1(), macData));
        return postForm(zalopayConfig.getQueryUrl(), form);
    }

    private PaymentGatewayReturnResponse processMomoPayload(Map<String, Object> payload, boolean allowReturnFallback) {
        boolean validSignature = verifyMomoSignature(payload);
        String txnRef = text(payload.get("orderId"));
        Integer resultCode = intValue(payload.get("resultCode"));
        boolean paymentSuccess = validSignature && resultCode != null && resultCode == 0;
        Map<String, Object> confirmationPayload = payload;
        WalletTransactions tx = findTxForUpdate(txnRef);

        if (allowReturnFallback && !paymentSuccess && tx != null && txnRef != null && !txnRef.isBlank()) {
            try {
                confirmationPayload = queryMomoOrder(txnRef);
                resultCode = intValue(confirmationPayload.get("resultCode"));
                paymentSuccess = resultCode != null && resultCode == 0;
                validSignature = resultCode != null;
            } catch (BadRequestException ignored) {
                confirmationPayload = payload;
            }
        }

        BigDecimal confirmedAmount = decimal(confirmationPayload.get("amount"));
        if (paymentSuccess && confirmedAmount == null && tx != null) {
            confirmedAmount = tx.getCashAmount();
        }
        String message = processTopUp(tx, PaymentGatewayProvider.MOMO, confirmedAmount, paymentSuccess);
        boolean success = tx != null && WalletTransactionStatus.COMPLETED.getValue().equalsIgnoreCase(tx.getStatus());
        return PaymentGatewayReturnResponse.builder()
                .gateway(PaymentGatewayProvider.MOMO.getValue())
                .validSignature(validSignature)
                .success(success)
                .txnRef(txnRef)
                .transactionRef(txnRef)
                .amount(text(confirmationPayload.get("amount")))
                .responseCode(text(confirmationPayload.get("resultCode")))
                .transactionStatus(tx == null ? null : tx.getStatus())
                .transactionNo(text(confirmationPayload.get("transId")))
                .message(validSignature ? message : "Invalid signature")
                .transaction(toResponse(tx))
                .build();
    }


    private String processTopUp(WalletTransactions tx, PaymentGatewayProvider provider, BigDecimal amount, boolean paymentSuccess) {
        if (tx == null || !WalletTransactionType.TOPUP.getValue().equalsIgnoreCase(tx.getTxType()) || !provider.getValue().equalsIgnoreCase(tx.getRefType())) {
            return "Payment transaction not found";
        }
        if (amount == null || amount.compareTo(tx.getCashAmount()) != 0) {
            return "Payment amount does not match transaction";
        }
        if (!WalletTransactionStatus.PENDING.getValue().equalsIgnoreCase(tx.getStatus())) {
            return paymentSuccess ? "Payment already confirmed" : "Payment already processed";
        }
        if (!paymentSuccess) {
            markFailed(tx);
            return "Payment failed";
        }
        Wallets wallet = walletsRepository.findFirstById(tx.getWallets().getId()).orElseThrow(() -> new BadRequestException("Wallet not found"));
        validateWalletActive(wallet);
        BigDecimal before = money(wallet.getPointBalance());
        BigDecimal after = before.add(tx.getPointsAmount());
        wallet.setPointBalance(after);
        tx.setPointsBefore(before);
        tx.setPointsAfter(after);
        tx.setStatus(WalletTransactionStatus.COMPLETED.getValue());
        walletsRepository.save(wallet);
        walletTransactionsRepository.save(tx);
        return "Payment success";
    }

    private WalletTransactions createPendingTopUp(Wallets wallet, Users user, BigDecimal cashAmount, PaymentGatewayProvider provider) {
        BigDecimal balance = money(wallet.getPointBalance());
        WalletTransactions tx = WalletTransactions.builder()
                .wallets(wallet)
                .users(user)
                .txType(WalletTransactionType.TOPUP.getValue())
                .cashAmount(cashAmount)
                .pointsAmount(cashAmount.multiply(DEFAULT_EXCHANGE_RATE))
                .exchangeRate(DEFAULT_EXCHANGE_RATE)
                .pointsBefore(balance)
                .pointsAfter(balance)
                .status(WalletTransactionStatus.PENDING.getValue())
                .refType(provider.getValue())
                .createdBy(user)
                .createdAt(Instant.now())
                .build();
        WalletTransactions saved = walletTransactionsRepository.save(tx);
        saved.setRefId(saved.getId());
        return walletTransactionsRepository.save(saved);
    }

    private boolean verifyMomoSignature(Map<String, Object> payload) {
        String signature = text(payload.get("signature"));
        if (signature == null || signature.isBlank()) {
            return false;
        }
        String raw = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + blank(payload.get("amount"))
                + "&extraData=" + blank(payload.get("extraData"))
                + "&message=" + blank(payload.get("message"))
                + "&orderId=" + blank(payload.get("orderId"))
                + "&orderInfo=" + blank(payload.get("orderInfo"))
                + "&orderType=" + blank(payload.get("orderType"))
                + "&partnerCode=" + blank(payload.get("partnerCode"))
                + "&payType=" + blank(payload.get("payType"))
                + "&requestId=" + blank(payload.get("requestId"))
                + "&responseTime=" + blank(payload.get("responseTime"))
                + "&resultCode=" + blank(payload.get("resultCode"))
                + "&transId=" + blank(payload.get("transId"));
        return PaymentGatewayUtil.hmacSHA256(momoConfig.getSecretKey(), raw).equalsIgnoreCase(signature);
    }

    private Map<String, Object> postJson(String url, Map<String, Object> payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(payload)))
                    .build();
            return readJson(HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body());
        } catch (Exception ex) {
            throw new BadRequestException("Cannot call payment gateway: " + ex.getMessage());
        }
    }

    private Map<String, Object> postForm(String url, Map<String, String> payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(PaymentGatewayUtil.formEncode(payload)))
                    .build();
            return readJson(HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body());
        } catch (Exception ex) {
            throw new BadRequestException("Cannot call payment gateway: " + ex.getMessage());
        }
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByUsername(username).orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private void validateSpectator(Users user) {
        if (!RoleType.SPECTATOR.getValue().equalsIgnoreCase(user.getRoleType())) {
            throw new UnauthorizedException("Only spectator can top up wallet");
        }
    }

    private Wallets getOrCreateWallet(Users user) {
        Wallets wallet = walletsRepository.findByUsersId(user.getId()).orElseGet(() -> walletsRepository.save(Wallets.builder()
                .users(user)
                .pointBalance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE.getValue())
                .createdAt(Instant.now())
                .build()));
        validateWalletActive(wallet);
        return wallet;
    }

    private WalletTransactions findTx(String txnRef) {
        Integer id = txId(txnRef);
        return id == null ? null : walletTransactionsRepository.findById(id).orElse(null);
    }

    private WalletTransactions findTxForUpdate(String txnRef) {
        Integer id = txId(txnRef);
        return id == null ? null : walletTransactionsRepository.findFirstById(id).orElse(null);
    }

    private Integer txId(String txnRef) {
        if (txnRef == null || !txnRef.startsWith("TOPUP-")) {
            return null;
        }
        try {
            return Integer.valueOf(txnRef.substring("TOPUP-".length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String transactionRef(Integer id) {
        return "TOPUP-" + id;
    }

    private String extractZalopayTxnRef(String appTransId) {
        if (appTransId == null) {
            return null;
        }
        int index = appTransId.indexOf('_');
        return index < 0 ? null : appTransId.substring(index + 1);
    }

    private PaymentTransactionResponse toResponse(WalletTransactions tx) {
        if (tx == null) {
            return null;
        }
        return PaymentTransactionResponse.builder()
                .txId(tx.getId())
                .walletId(tx.getWallets() == null ? null : tx.getWallets().getId())
                .userId(tx.getUsers() == null ? null : tx.getUsers().getId())
                .txType(tx.getTxType())
                .cashAmount(tx.getCashAmount())
                .pointsAmount(tx.getPointsAmount())
                .exchangeRate(tx.getExchangeRate())
                .pointsBefore(tx.getPointsBefore())
                .pointsAfter(tx.getPointsAfter())
                .status(tx.getStatus())
                .refType(tx.getRefType())
                .refId(tx.getRefId())
                .createdBy(tx.getCreatedBy() == null ? null : tx.getCreatedBy().getId())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }

    private void markFailed(WalletTransactions tx) {
        tx.setStatus(WalletTransactionStatus.FAILED.getValue());
        walletTransactionsRepository.save(tx);
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

    private void validateWalletActive(Wallets wallet) {
        if (wallet.getStatus() == null || !WalletStatus.ACTIVE.getValue().equalsIgnoreCase(wallet.getStatus())) {
            throw new BadRequestException("Wallet is not active");
        }
    }

    private void validateMomoConfig() {
        require(momoConfig.getPartnerCode(), "MoMo partner code is not configured");
        require(momoConfig.getAccessKey(), "MoMo access key is not configured");
        require(momoConfig.getSecretKey(), "MoMo secret key is not configured");
        require(momoConfig.getCreateUrl(), "MoMo create url is not configured");
        require(momoConfig.getQueryUrl(), "MoMo query url is not configured");
        require(momoConfig.getRedirectUrl(), "MoMo redirect url is not configured");
        require(momoConfig.getIpnUrl(), "MoMo IPN url is not configured");
    }

    private void validateZalopayConfig() {
        require(zalopayConfig.getAppId(), "ZaloPay app id is not configured");
        require(zalopayConfig.getKey1(), "ZaloPay key1 is not configured");
        require(zalopayConfig.getKey2(), "ZaloPay key2 is not configured");
        require(zalopayConfig.getCreateUrl(), "ZaloPay create url is not configured");
        require(zalopayConfig.getQueryUrl(), "ZaloPay query url is not configured");
        require(zalopayConfig.getRedirectUrl(), "ZaloPay redirect url is not configured");
        require(zalopayConfig.getCallbackUrl(), "ZaloPay callback url is not configured");
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
    }

    private Map<String, Object> readJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() { });
        } catch (Exception ex) {
            throw new BadRequestException("Invalid payment gateway JSON response");
        }
    }

    private String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BadRequestException("Cannot build payment gateway payload");
        }
    }

    private Map<String, Object> firstValues(Map<String, String[]> parameterMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        parameterMap.forEach((key, values) -> {
            if (values != null && values.length > 0) {
                result.put(key, values[0]);
            }
        });
        return result;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String gatewayAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString();
    }

    private BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer intValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String blank(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}

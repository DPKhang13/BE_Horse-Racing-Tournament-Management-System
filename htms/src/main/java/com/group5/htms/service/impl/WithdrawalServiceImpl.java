package com.group5.htms.service.impl;

import com.group5.htms.dto.withdrawal.request.WithdrawalApproveRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalCreateRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalMarkPaidRequest;
import com.group5.htms.dto.withdrawal.request.WithdrawalRejectRequest;
import com.group5.htms.dto.withdrawal.response.WithdrawalResponse;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.entity.Withdrawals;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.WalletStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.enums.WithdrawalStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.UnauthorizedException;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.repository.WithdrawalsRepository;
import com.group5.htms.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE = new BigDecimal("0.001000");
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("10.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Locale VIETNAM_LOCALE = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter EMAIL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(VIETNAM_ZONE);

    private final UsersRepository usersRepository;
    private final WalletsRepository walletsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final WithdrawalsRepository withdrawalsRepository;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public WithdrawalResponse createWithdrawal(WithdrawalCreateRequest request) {
        Users user = getCurrentUser();
        validateSpectator(user);

        Wallets wallet = getLockedWallet(user.getId());
        validateWalletActive(wallet);

        BigDecimal requestedPoints = money(request.getPointsAmount());
        if (requestedPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Points amount must be greater than 0");
        }

        BigDecimal pointsBefore = money(wallet.getPointBalance());
        if (pointsBefore.compareTo(requestedPoints) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }

        BigDecimal grossCash = requestedPoints.divide(DEFAULT_EXCHANGE_RATE, 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = grossCash.multiply(DEFAULT_TAX_RATE).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal netCash = grossCash.subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pointsAfter = pointsBefore.subtract(requestedPoints).setScale(2, RoundingMode.HALF_UP);

        wallet.setPointBalance(pointsAfter);
        walletsRepository.save(wallet);

        WalletTransactions tx = walletTransactionsRepository.save(WalletTransactions.builder()
                .wallets(wallet)
                .users(user)
                .txType(WalletTransactionType.WITHDRAW.getValue())
                .cashAmount(grossCash)
                .pointsAmount(requestedPoints)
                .exchangeRate(DEFAULT_EXCHANGE_RATE)
                .pointsBefore(pointsBefore)
                .pointsAfter(pointsAfter)
                .status(WalletTransactionStatus.PENDING.getValue())
                .refType("withdrawal")
                .createdBy(user)
                .createdAt(Instant.now())
                .build());

        Withdrawals withdrawal = withdrawalsRepository.save(Withdrawals.builder()
                .transaction(tx)
                .users(user)
                .wallets(wallet)
                .requestedPoints(requestedPoints)
                .grossCashAmount(grossCash)
                .taxRate(DEFAULT_TAX_RATE)
                .taxAmount(taxAmount)
                .netCashAmount(netCash)
                .exchangeRate(DEFAULT_EXCHANGE_RATE)
                .status(WithdrawalStatus.PENDING.getValue())
                .createdAt(Instant.now())
                .build());

        tx.setRefId(withdrawal.getId());
        walletTransactionsRepository.save(tx);

        return toResponse(withdrawal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalResponse> getMyWithdrawals() {
        Users user = getCurrentUser();
        return withdrawalsRepository.findByUsersIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WithdrawalResponse getMyWithdrawal(Integer withdrawalId) {
        Users user = getCurrentUser();
        Withdrawals withdrawal = withdrawalsRepository.findByIdAndUsersId(withdrawalId, user.getId())
                .orElseThrow(() -> new BadRequestException("Withdrawal not found"));
        return toResponse(withdrawal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalResponse> getAllWithdrawals(String status) {
        List<Withdrawals> withdrawals = status == null || status.isBlank()
                ? withdrawalsRepository.findAllByOrderByCreatedAtDesc()
                : withdrawalsRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc(status.trim());

        return withdrawals.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WithdrawalResponse approveWithdrawal(Integer withdrawalId, WithdrawalApproveRequest request) {
        Users admin = getCurrentUser();
        Withdrawals withdrawal = getLockedWithdrawal(withdrawalId);

        if (!WithdrawalStatus.PENDING.equalsValue(withdrawal.getStatus())) {
            throw new BadRequestException("Only pending withdrawals can be approved");
        }

        withdrawal.setStatus(WithdrawalStatus.APPROVED.getValue());
        withdrawal.setApprovedBy(admin);
        withdrawal.setApprovedAt(Instant.now());
        withdrawal.setPayoutLocation(cleanRequired(request.getPayoutLocation(), "Payout location is required"));
        withdrawal.setPayoutCounter(cleanRequired(request.getPayoutCounter(), "Payout counter is required"));
        withdrawal.setPickupCode(generatePickupCode());

        Withdrawals saved = withdrawalsRepository.save(withdrawal);
        sendPickupCodeEmailQuietly(saved);
        return toResponse(withdrawalsRepository.save(saved));
    }

    @Override
    @Transactional
    public WithdrawalResponse rejectWithdrawal(Integer withdrawalId, WithdrawalRejectRequest request) {
        Users admin = getCurrentUser();
        Withdrawals withdrawal = getLockedWithdrawal(withdrawalId);

        if (WithdrawalStatus.REJECTED.equalsValue(withdrawal.getStatus())
                || WithdrawalStatus.PAID.equalsValue(withdrawal.getStatus())) {
            throw new BadRequestException("Withdrawal cannot be rejected");
        }

        WalletTransactions tx = getLockedTransaction(withdrawal);
        if (!WalletTransactionStatus.PENDING.getValue().equalsIgnoreCase(tx.getStatus())) {
            throw new BadRequestException("Withdrawal transaction is not pending");
        }

        Wallets wallet = walletsRepository.findFirstById(withdrawal.getWallets().getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found"));
        validateWalletActive(wallet);

        BigDecimal pointsBeforeRefund = money(wallet.getPointBalance());
        BigDecimal pointsAfterRefund = pointsBeforeRefund.add(withdrawal.getRequestedPoints()).setScale(2, RoundingMode.HALF_UP);
        wallet.setPointBalance(pointsAfterRefund);

        tx.setStatus(WalletTransactionStatus.CANCELLED.getValue());
        tx.setPointsAfter(pointsAfterRefund);

        withdrawal.setStatus(WithdrawalStatus.REJECTED.getValue());
        withdrawal.setRejectReason(cleanRequired(request.getRejectReason(), "Reject reason is required"));
        withdrawal.setRejectedBy(admin);
        withdrawal.setRejectedAt(Instant.now());

        walletsRepository.save(wallet);
        walletTransactionsRepository.save(tx);
        return toResponse(withdrawalsRepository.save(withdrawal));
    }

    @Override
    @Transactional
    public WithdrawalResponse markWithdrawalPaid(Integer withdrawalId, WithdrawalMarkPaidRequest request) {
        Users admin = getCurrentUser();
        Withdrawals withdrawal = getLockedWithdrawal(withdrawalId);

        if (!WithdrawalStatus.APPROVED.equalsValue(withdrawal.getStatus())) {
            throw new BadRequestException("Only approved withdrawals can be marked as paid");
        }

        String pickupCode = cleanRequired(request.getPickupCode(), "Pickup code is required");
        if (withdrawal.getPickupCode() == null || !withdrawal.getPickupCode().equalsIgnoreCase(pickupCode)) {
            throw new BadRequestException("Invalid pickup code");
        }

        WalletTransactions tx = getLockedTransaction(withdrawal);
        if (!WalletTransactionStatus.PENDING.getValue().equalsIgnoreCase(tx.getStatus())) {
            throw new BadRequestException("Withdrawal transaction is not pending");
        }

        Instant now = Instant.now();
        withdrawal.setStatus(WithdrawalStatus.PAID.getValue());
        withdrawal.setPaidBy(admin);
        withdrawal.setPaidAt(now);
        withdrawal.setPaymentNote(clean(request.getPaymentNote()));
        generateInvoiceIfNeeded(withdrawal, now);

        tx.setStatus(WalletTransactionStatus.COMPLETED.getValue());
        walletTransactionsRepository.save(tx);

        Withdrawals saved = withdrawalsRepository.save(withdrawal);
        sendInvoiceEmailQuietly(saved);
        return toResponse(withdrawalsRepository.save(saved));
    }

    @Override
    @Transactional
    public WithdrawalResponse resendInvoice(Integer withdrawalId) {
        Withdrawals withdrawal = getLockedWithdrawal(withdrawalId);
        if (!WithdrawalStatus.PAID.equalsValue(withdrawal.getStatus())) {
            throw new BadRequestException("Only paid withdrawals can resend invoice");
        }

        generateInvoiceIfNeeded(withdrawal, Instant.now());
        Withdrawals saved = withdrawalsRepository.save(withdrawal);
        sendInvoiceEmailQuietly(saved);
        return toResponse(withdrawalsRepository.save(saved));
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private void validateSpectator(Users user) {
        if (!RoleType.SPECTATOR.getValue().equalsIgnoreCase(user.getRoleType())) {
            throw new UnauthorizedException("Only spectator can withdraw wallet balance");
        }
    }

    private Wallets getLockedWallet(Integer userId) {
        Wallets wallet = walletsRepository.findByUsersId(userId)
                .orElseThrow(() -> new BadRequestException("Wallet not found"));
        return walletsRepository.findFirstById(wallet.getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found"));
    }

    private Withdrawals getLockedWithdrawal(Integer withdrawalId) {
        if (withdrawalId == null) {
            throw new BadRequestException("Withdrawal id is required");
        }
        return withdrawalsRepository.findFirstById(withdrawalId)
                .orElseThrow(() -> new BadRequestException("Withdrawal not found"));
    }

    private WalletTransactions getLockedTransaction(Withdrawals withdrawal) {
        return walletTransactionsRepository.findFirstById(withdrawal.getTransaction().getId())
                .orElseThrow(() -> new BadRequestException("Withdrawal transaction not found"));
    }

    private void validateWalletActive(Wallets wallet) {
        if (wallet.getStatus() == null || !WalletStatus.ACTIVE.getValue().equalsIgnoreCase(wallet.getStatus())) {
            throw new BadRequestException("Wallet is not active");
        }
    }

    private String generatePickupCode() {
        String pickupCode;
        do {
            pickupCode = "WD" + String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        } while (withdrawalsRepository.existsByPickupCodeIgnoreCase(pickupCode));
        return pickupCode;
    }

    private void generateInvoiceIfNeeded(Withdrawals withdrawal, Instant now) {
        if (withdrawal.getInvoiceNumber() == null || withdrawal.getInvoiceNumber().isBlank()) {
            withdrawal.setInvoiceNumber("WD-" + withdrawal.getId() + "-" + now.toEpochMilli());
        }
        if (withdrawal.getInvoiceGeneratedAt() == null) {
            withdrawal.setInvoiceGeneratedAt(now);
        }
    }

    private void sendPickupCodeEmailQuietly(Withdrawals withdrawal) {
        Users user = withdrawal.getUsers();
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("HTMS Withdrawal Approved - Pickup Code " + withdrawal.getPickupCode());
            message.setText("""
                    Your withdrawal request has been approved.

                    Pickup code: %s
                    Payout location: %s
                    Payout counter: %s
                    Net cash amount: %s
                    Tax: %s
                    Approved at: %s

                    Please bring this pickup code to the payout counter for verification.
                    """.formatted(
                    withdrawal.getPickupCode(),
                    withdrawal.getPayoutLocation(),
                    withdrawal.getPayoutCounter(),
                    formatVnd(withdrawal.getNetCashAmount()),
                    formatVnd(withdrawal.getTaxAmount()),
                    formatDateTime(withdrawal.getApprovedAt())
            ));
            mailSender.send(message);
            withdrawal.setEmailSentTo(user.getEmail());
        } catch (Exception ignored) {
            withdrawal.setEmailSentTo(user.getEmail());
        }
    }

    private void sendInvoiceEmailQuietly(Withdrawals withdrawal) {
        Users user = withdrawal.getUsers();
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("HTMS Withdrawal Invoice " + withdrawal.getInvoiceNumber());
            message.setText("""
                    Withdrawal invoice: %s
                    Customer: %s
                    Gross amount: %s
                    Tax: %s
                    Net paid: %s
                    Pickup code: %s
                    Payout location: %s
                    Payout counter: %s
                    Paid at: %s
                    """.formatted(
                    withdrawal.getInvoiceNumber(),
                    user.getFullName(),
                    formatVnd(withdrawal.getGrossCashAmount()),
                    formatVnd(withdrawal.getTaxAmount()),
                    formatVnd(withdrawal.getNetCashAmount()),
                    withdrawal.getPickupCode(),
                    withdrawal.getPayoutLocation(),
                    withdrawal.getPayoutCounter(),
                    formatDateTime(withdrawal.getPaidAt())
            ));
            mailSender.send(message);
            withdrawal.setEmailSentTo(user.getEmail());
            withdrawal.setInvoiceEmailedAt(Instant.now());
        } catch (Exception ignored) {
            withdrawal.setEmailSentTo(user.getEmail());
        }
    }

    private WithdrawalResponse toResponse(Withdrawals withdrawal) {
        Users user = withdrawal.getUsers();
        WalletTransactions tx = withdrawal.getTransaction();
        return WithdrawalResponse.builder()
                .withdrawalId(withdrawal.getId())
                .txId(tx == null ? null : tx.getId())
                .userId(user == null ? null : user.getId())
                .username(user == null ? null : user.getUsername())
                .userFullName(user == null ? null : user.getFullName())
                .userEmail(user == null ? null : user.getEmail())
                .walletId(withdrawal.getWallets() == null ? null : withdrawal.getWallets().getId())
                .requestedPoints(withdrawal.getRequestedPoints())
                .grossCashAmount(withdrawal.getGrossCashAmount())
                .taxRate(withdrawal.getTaxRate())
                .taxAmount(withdrawal.getTaxAmount())
                .netCashAmount(withdrawal.getNetCashAmount())
                .exchangeRate(withdrawal.getExchangeRate())
                .pickupCode(withdrawal.getPickupCode())
                .payoutLocation(withdrawal.getPayoutLocation())
                .payoutCounter(withdrawal.getPayoutCounter())
                .status(withdrawal.getStatus())
                .approvedBy(withdrawal.getApprovedBy() == null ? null : withdrawal.getApprovedBy().getId())
                .approvedAt(withdrawal.getApprovedAt())
                .rejectedBy(withdrawal.getRejectedBy() == null ? null : withdrawal.getRejectedBy().getId())
                .rejectedAt(withdrawal.getRejectedAt())
                .paidBy(withdrawal.getPaidBy() == null ? null : withdrawal.getPaidBy().getId())
                .paidAt(withdrawal.getPaidAt())
                .rejectReason(withdrawal.getRejectReason())
                .paymentNote(withdrawal.getPaymentNote())
                .invoiceNumber(withdrawal.getInvoiceNumber())
                .invoiceUrl(withdrawal.getInvoiceUrl())
                .invoiceGeneratedAt(withdrawal.getInvoiceGeneratedAt())
                .invoiceEmailedAt(withdrawal.getInvoiceEmailedAt())
                .emailSentTo(withdrawal.getEmailSentTo())
                .createdAt(withdrawal.getCreatedAt())
                .build();
    }

    private String formatVnd(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value == null ? BigDecimal.ZERO : value);
    }

    private String formatDateTime(Instant value) {
        return value == null ? "N/A" : EMAIL_DATE_TIME_FORMAT.format(value);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String cleanRequired(String value, String message) {
        String cleaned = clean(value);
        if (cleaned == null) {
            throw new BadRequestException(message);
        }
        return cleaned;
    }
}
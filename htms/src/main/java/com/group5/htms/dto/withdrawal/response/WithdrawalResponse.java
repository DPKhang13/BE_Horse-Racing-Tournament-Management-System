package com.group5.htms.dto.withdrawal.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class WithdrawalResponse {
    private Integer withdrawalId;
    private Integer txId;
    private Integer userId;
    private String username;
    private String userFullName;
    private String userEmail;
    private Integer walletId;
    private BigDecimal requestedPoints;
    private BigDecimal grossCashAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal netCashAmount;
    private BigDecimal exchangeRate;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String status;
    private Integer approvedBy;
    private Instant approvedAt;
    private Integer rejectedBy;
    private Instant rejectedAt;
    private Integer paidBy;
    private Instant paidAt;
    private String rejectReason;
    private String bankTransactionCode;
    private String paymentNote;
    private String invoiceNumber;
    private String invoiceUrl;
    private Instant invoiceGeneratedAt;
    private Instant invoiceEmailedAt;
    private String emailSentTo;
    private Instant createdAt;
}
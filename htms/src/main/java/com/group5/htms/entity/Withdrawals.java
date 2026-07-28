package com.group5.htms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"withdrawals\"")
public class Withdrawals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawal_id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tx_id", nullable = false, unique = true)
    private WalletTransactions transaction;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallets wallets;

    @NotNull
    @Column(name = "requested_points", nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedPoints;

    @NotNull
    @Column(name = "gross_cash_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal grossCashAmount;

    @NotNull
    @ColumnDefault("10.00")
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @NotNull
    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @NotNull
    @Column(name = "net_cash_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal netCashAmount;

    @Column(name = "exchange_rate", precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Size(max = 100)
    @NotNull
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Size(max = 50)
    @NotNull
    @Column(name = "bank_account_number", nullable = false, length = 50)
    private String bankAccountNumber;

    @Size(max = 100)
    @NotNull
    @Column(name = "bank_account_name", nullable = false, length = 100)
    private String bankAccountName;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'pending'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private Users rejectedBy;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private Users paidBy;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Size(max = 255)
    @Column(name = "reject_reason")
    private String rejectReason;

    @Size(max = 100)
    @Column(name = "bank_transaction_code", length = 100)
    private String bankTransactionCode;

    @Size(max = 255)
    @Column(name = "payment_note")
    private String paymentNote;

    @Size(max = 50)
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "invoice_url", length = Integer.MAX_VALUE)
    private String invoiceUrl;

    @Column(name = "invoice_generated_at")
    private Instant invoiceGeneratedAt;

    @Column(name = "invoice_emailed_at")
    private Instant invoiceEmailedAt;

    @Size(max = 100)
    @Column(name = "email_sent_to", length = 100)
    private String emailSentTo;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
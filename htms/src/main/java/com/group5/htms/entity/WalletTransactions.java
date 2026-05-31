package com.group5.htms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
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
@Table(name = "\"WalletTransactions\"")
public class WalletTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallets wallets;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spectator_role_id", nullable = false)
    private Roles spectatorRoles;

    @Size(max = 20)
    @NotNull
    @Column(name = "tx_type", nullable = false, length = 20)
    private String txType;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "cash_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal cashAmount;

    @NotNull
    @Column(name = "points_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal pointsAmount;

    @Column(name = "exchange_rate", precision = 18, scale = 2)
    private BigDecimal exchangeRate;

    @NotNull
    @Column(name = "points_before", nullable = false, precision = 18, scale = 2)
    private BigDecimal pointsBefore;

    @NotNull
    @Column(name = "points_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal pointsAfter;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'pending'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 50)
    @Column(name = "ref_type", length = 50)
    private String refType;

    @Column(name = "ref_id")
    private Integer refId;

    /*
     * Payment gateway provider.
     * Với VNPay: "vnpay".
     * Sau này có thể thêm "zalopay".
     */
    @Size(max = 30)
    @Column(name = "gateway_provider", length = 30)
    private String gatewayProvider;

    /*
     * Mã giao dịch gửi sang VNPay qua vnp_TxnRef.
     *
     * Không dùng tx_id trần.
     * Format nên là: TOPUP-{txId}-{random}
     *
     * Ví dụ: TOPUP-25-A8F3K2D9
     */
    @Size(max = 100)
    @Column(name = "gateway_txn_ref", length = 100)
    private String gatewayTxnRef;

    /*
     * Mã giao dịch do VNPay trả về: vnp_TransactionNo.
     */
    @Size(max = 100)
    @Column(name = "gateway_transaction_no", length = 100)
    private String gatewayTransactionNo;

    /*
     * VNPay vnp_ResponseCode.
     * "00" thường là thành công.
     */
    @Size(max = 20)
    @Column(name = "gateway_response_code", length = 20)
    private String gatewayResponseCode;

    /*
     * VNPay vnp_TransactionStatus.
     * "00" thường là giao dịch thành công.
     */
    @Size(max = 20)
    @Column(name = "gateway_transaction_status", length = 20)
    private String gatewayTransactionStatus;

    /*
     * Ngân hàng/kênh thanh toán VNPay trả về.
     */
    @Size(max = 50)
    @Column(name = "gateway_bank_code", length = 50)
    private String gatewayBankCode;

    /*
     * Thời gian thanh toán VNPay trả về dạng string.
     * Ví dụ: yyyyMMddHHmmss.
     */
    @Size(max = 50)
    @Column(name = "gateway_pay_date", length = 50)
    private String gatewayPayDate;

    /*
     * Raw params từ VNPay để debug/audit.
     *
     * Lưu ý:
     * - Không expose field này ra API public.
     * - Không log ra console.
     */
    @Column(name = "gateway_raw_response", columnDefinition = "TEXT")
    private String gatewayRawResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @ToString.Exclude
    private Users createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.cashAmount == null) {
            this.cashAmount = BigDecimal.ZERO;
        }

        if (this.pointsAmount == null) {
            this.pointsAmount = BigDecimal.ZERO;
        }

        if (this.exchangeRate == null) {
            this.exchangeRate = BigDecimal.ONE;
        }

        if (this.pointsBefore == null) {
            this.pointsBefore = BigDecimal.ZERO;
        }

        if (this.pointsAfter == null) {
            this.pointsAfter = BigDecimal.ZERO;
        }

        if (this.status == null || this.status.isBlank()) {
            this.status = "pending";
        }

        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
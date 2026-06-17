package com.group5.htms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "\"wallet_transactions\"")
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
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

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

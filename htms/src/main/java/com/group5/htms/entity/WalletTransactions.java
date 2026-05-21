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
    @ColumnDefault("'completed'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 50)
    @Column(name = "ref_type", length = 50)
    private String refType;

    @Column(name = "ref_id")
    private Integer refId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
package com.group5.htms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "\"bets\"")
public class Bets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bet_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private BetOptions option;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "bet_type", nullable = false)
    private Boolean betType;

    @NotNull
    @Column(name = "bet_points", nullable = false, precision = 18, scale = 2)
    private BigDecimal betPoints;

    @NotNull
    @Column(name = "bet_rate", nullable = false, precision = 8, scale = 2)
    private BigDecimal betRate;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "reward_points", nullable = false, precision = 18, scale = 2)
    private BigDecimal rewardPoints;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'pending'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull
    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Column(name = "settled_at")
    private Instant settledAt;
}

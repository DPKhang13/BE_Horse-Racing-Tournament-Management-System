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
@Table(name = "\"Bets\"")
public class Bets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bet_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spectator_role_id", nullable = false)
    private Roles spectatorRoles;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private JockeyHorseAssignments assignment;

    @Size(max = 30)
    @NotNull
    @ColumnDefault("'winner'")
    @Column(name = "market_type", nullable = false, length = 30)
    private String marketType;

    @Column(name = "predicted_position")
    private Integer predictedPosition;

    @NotNull
    @Column(name = "stake_points", nullable = false, precision = 18, scale = 2)
    private BigDecimal stakePoints;

    @NotNull
    @Column(name = "odds_decimal", nullable = false, precision = 8, scale = 2)
    private BigDecimal oddsDecimal;

    @Column(name = "potential_payout_points", precision = 18, scale = 2)
    private BigDecimal potentialPayoutPoints;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "payout_points", nullable = false, precision = 18, scale = 2)
    private BigDecimal payoutPoints;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settled_by")
    private Users settledBy;

    @Size(max = 20)
    @Column(name = "settled_type", length = 20)
    private String settledType;

}
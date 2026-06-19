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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
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
@Table(
        name = "\"bet_options\"",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_option_race_horse",
                columnNames = {"race_id", "horse_id"}
        )
)
public class BetOptions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Races races;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private JockeyHorseAssignments assignment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false)
    private Horses horses;

    @NotNull
    @Column(name = "current_rate", nullable = false, precision = 8, scale = 2)
    private BigDecimal currentRate;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "total_bet_points", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalBetPoints;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "total_bet_count", nullable = false)
    private Integer totalBetCount;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

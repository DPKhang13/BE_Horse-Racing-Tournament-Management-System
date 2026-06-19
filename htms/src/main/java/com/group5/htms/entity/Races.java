package com.group5.htms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"races\"")
public class Races {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "race_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TournamentSchedules schedule;

    @Size(max = 200)
    @NotNull
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotNull
    @Column(name = "race_number", nullable = false)
    private Integer raceNumber;

    @Column(name = "rank_group", length = 1)
    private String rankGroup;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "lap_count", nullable = false)
    private Integer lapCount;

    @NotNull
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "prediction_closes_at")
    private Instant predictionClosesAt;

    @NotNull
    @Column(name = "distance_m", nullable = false)
    private Double distanceM;

    @Size(max = 50)
    @Column(name = "track_type", length = 50)
    private String trackType;

    @NotNull
    @ColumnDefault("8")
    @Column(name = "max_horses", nullable = false)
    private Integer maxHorses;

    @NotNull
    @ColumnDefault("3")
    @Column(name = "max_referees", nullable = false)
    private Integer maxReferees;

    @Column(name = "point_rule_note", length = Integer.MAX_VALUE)
    private String pointRuleNote;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'scheduled'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

}

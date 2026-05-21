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
@Table(name = "\"RaceResults\"")
public class RaceResults {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private JockeyHorseAssignments assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private RefereeReports report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prize_id")
    private PrizeDistributions prize;

    @Column(name = "finish_position")
    private Integer finishPosition;

    @Column(name = "finish_time_sec", precision = 10, scale = 3)
    private BigDecimal finishTimeSec;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_disqualified", nullable = false)
    private Boolean isDisqualified = false;

    @Size(max = 500)
    @Column(name = "disqualify_reason", length = 500)
    private String disqualifyReason;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'draft'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

}
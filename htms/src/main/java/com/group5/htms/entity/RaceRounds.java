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
@Table(name = "\"race_rounds\"")
public class RaceRounds {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id", nullable = false)
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
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @NotNull
    @Column(name = "\"position\"", nullable = false)
    private Integer position;

    @Column(name = "lap_time_sec", precision = 10, scale = 3)
    private BigDecimal lapTimeSec;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}

package com.group5.htms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"TournamentSchedules\"")
public class TournamentSchedules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournaments tournaments;

    @NotNull
    @Column(name = "race_date", nullable = false)
    private LocalDate raceDate;

    @NotNull
    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Size(max = 200)
    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "note", length = Integer.MAX_VALUE)
    private String note;

}
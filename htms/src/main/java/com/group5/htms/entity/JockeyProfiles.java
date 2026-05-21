package com.group5.htms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"JockeyProfiles\"")
public class JockeyProfiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jockey_profile_id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Roles roles;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "ranking_points", nullable = false)
    private Integer rankingPoints;

    @Column(name = "total_wins")
    private Integer totalWins;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'available'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

}
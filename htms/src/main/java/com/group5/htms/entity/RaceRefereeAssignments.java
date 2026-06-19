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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(
        name = "\"race_referee_assignments\"",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_ref_assign_race_referee",
                        columnNames = {"race_id", "referee_id"}
                ),
                @UniqueConstraint(
                        name = "uq_ref_assign_race_role",
                        columnNames = {"race_id", "referee_role"}
                )
        }
)
public class RaceRefereeAssignments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ref_assign_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Races races;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referee_id", nullable = false)
    private RefereeProfiles referee;

    @Size(max = 50)
    @NotNull
    @Column(name = "referee_role", nullable = false, length = 50)
    private String refereeRole;

    @NotNull
    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;
}

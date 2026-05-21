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
@Table(name = "\"RaceRegistrations\"")
public class RaceRegistrations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reg_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournaments tournaments;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Races races;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false)
    private Horses horses;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_role_id", nullable = false)
    private Roles ownerRoles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jockey_role_id")
    private Roles jockeyRoles;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'pending'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'pending'")
    @Column(name = "owner_confirmation_status", nullable = false, length = 20)
    private String ownerConfirmationStatus;

    @Column(name = "owner_confirmed_at")
    private Instant ownerConfirmedAt;

    @NotNull
    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

}
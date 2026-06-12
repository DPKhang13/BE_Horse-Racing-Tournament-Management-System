package com.group5.htms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
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

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"jockey_profiles\"")
public class JockeyProfiles {
    @Id
    @Column(name = "jockey_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jockey_id", nullable = false)
    @ToString.Exclude
    private Users users;

    @Size(max = 50)
    @Column(name = "license_number", length = 50)
    private String licenseNumber;

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

    @PrePersist
    public void prePersist() {
        if (this.rankingPoints == null) {
            this.rankingPoints = 0;
        }

        if (this.experienceYears == null) {
            this.experienceYears = 0;
        }

        if (this.status == null || this.status.isBlank()) {
            this.status = "available";
        }
    }
}

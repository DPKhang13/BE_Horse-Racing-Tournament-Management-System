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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "\"RefereeReports\"")
public class RefereeReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Races races;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referee_id", nullable = false)
    private RefereeProfiles referee;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'individual'")
    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType;

    @Column(name = "inspection_notes", length = Integer.MAX_VALUE)
    private String inspectionNotes;

    @Column(name = "violation_notes", length = Integer.MAX_VALUE)
    private String violationNotes;

    @Column(name = "result_notes", length = Integer.MAX_VALUE)
    private String resultNotes;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'clean'")
    @Column(name = "verdict", nullable = false, length = 20)
    private String verdict;

    @NotNull
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
}

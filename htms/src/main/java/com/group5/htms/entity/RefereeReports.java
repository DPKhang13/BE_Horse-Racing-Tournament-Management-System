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
@Table(name = "\"RefereeReports\"")
public class RefereeReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ref_assign_id", nullable = false)
    private RaceRefereeAssignments refAssign;

    @Size(max = 200)
    @Column(name = "point_rule", length = 200)
    private String pointRule;

    @Column(name = "inspection_notes", length = Integer.MAX_VALUE)
    private String inspectionNotes;

    @Column(name = "summary", length = Integer.MAX_VALUE)
    private String summary;

    @Column(name = "violations", length = Integer.MAX_VALUE)
    private String violations;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'clean'")
    @Column(name = "verdict", nullable = false, length = 20)
    private String verdict;

    @NotNull
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

}
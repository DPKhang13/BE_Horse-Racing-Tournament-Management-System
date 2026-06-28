package com.group5.htms.dto.refereereport.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class RefereeReportResponse {
    private Integer reportId;
    private Integer raceId;
    private String raceName;
    private Integer refereeId;
    private String refereeFullName;
    private String reportType;
    private String inspectionNotes;
    private String violationNotes;
    private String resultNotes;
    private String verdict;
    private Instant submittedAt;
}

package com.group5.htms.dto.refereereport.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefereeReportCreateRequest {
    @Size(max = 20, message = "Report type must not exceed 20 characters")
    private String reportType;

    private String inspectionNotes;

    private String violationNotes;

    private String resultNotes;

    @Size(max = 20, message = "Verdict must not exceed 20 characters")
    private String verdict;
}

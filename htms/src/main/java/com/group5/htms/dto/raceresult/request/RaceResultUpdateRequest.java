package com.group5.htms.dto.raceresult.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class RaceResultUpdateRequest {
    private Integer assignmentId;
    private Integer reportId;
    private Integer finalRound;

    @Min(value = 1, message = "Finish position must be greater than or equal to 1")
    private Integer finishPosition;

    @DecimalMin(value = "0.0", inclusive = false, message = "Finish time must be greater than 0")
    private BigDecimal finishTimeSec;

    @Min(value = 0, message = "Points awarded must be greater than or equal to 0")
    private Integer pointsAwarded;

    private Boolean isDisqualified;

    @Size(max = 500, message = "Disqualify reason must not exceed 500 characters")
    private String disqualifyReason;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Instant recordedAt;
    private Instant publishedAt;
}

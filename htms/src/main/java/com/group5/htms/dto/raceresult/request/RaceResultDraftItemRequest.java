package com.group5.htms.dto.raceresult.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RaceResultDraftItemRequest {
    @NotNull(message = "Assignment id is required")
    private Integer assignmentId;

    @Min(value = 1, message = "Finish position must be greater than or equal to 1")
    private Integer finishPosition;

    @DecimalMin(value = "0.0", inclusive = false, message = "Finish time must be greater than 0")
    private BigDecimal finishTimeSec;

    private Boolean isDisqualified;

    @Size(max = 500, message = "Disqualify reason must not exceed 500 characters")
    private String disqualifyReason;
}

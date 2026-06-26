package com.group5.htms.dto.raceround.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class RaceRoundUpdateRequest {
    private Integer assignmentId;

    @Min(value = 1, message = "Round number must be greater than or equal to 1")
    private Integer roundNumber;

    @Min(value = 1, message = "Position must be greater than or equal to 1")
    private Integer position;

    @DecimalMin(value = "0.0", inclusive = false, message = "Lap time must be greater than 0")
    private BigDecimal lapTimeSec;

    private Instant recordedAt;
}

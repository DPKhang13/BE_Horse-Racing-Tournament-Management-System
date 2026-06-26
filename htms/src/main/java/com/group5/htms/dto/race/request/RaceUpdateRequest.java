package com.group5.htms.dto.race.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceUpdateRequest {

    @Size(max = 200, message = "Race name must not exceed 200 characters")
    private String name;

    @Min(value = 1, message = "Race number must be greater than or equal to 1")
    private Integer raceNumber;

    @Size(max = 1, message = "Rank group must not exceed 1 character")
    private String rankGroup;

    @Min(value = 1, message = "Lap count must be greater than or equal to 1")
    private Integer lapCount;

    private Instant scheduledAt;

    private Instant predictionClosesAt;

    @DecimalMin(value = "1.0", inclusive = true, message = "Distance must be greater than 0")
    private Double distanceM;

    @Size(max = 50, message = "Track type must not exceed 50 characters")
    private String trackType;

    @Min(value = 1, message = "Max horses must be greater than or equal to 1")
    private Integer maxHorses;

    @Min(value = 1, message = "Max referees must be greater than or equal to 1")
    private Integer maxReferees;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;
}

package com.group5.htms.dto.horse.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class HorseUpdateRequest {
    @Schema(hidden = true)
    private Integer ownerId;

    @Size(max = 100, message = "Horse name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Breed must not exceed 100 characters")
    private String breed;

    @Min(value = 0, message = "Age must be greater than or equal to 0")
    private Integer age;

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    private BigDecimal weightKg;

    private String rankGroup;

    @Min(value = 0, message = "Ranking points must be greater than or equal to 0")
    private Integer rankingPoints;

    private String avatarUrl;

    @Min(value = 0, message = "Total wins must be greater than or equal to 0")
    private Integer totalWins;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Instant registeredAt;
}

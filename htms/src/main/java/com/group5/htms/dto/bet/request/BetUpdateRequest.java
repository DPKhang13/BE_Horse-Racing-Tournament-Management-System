package com.group5.htms.dto.bet.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class BetUpdateRequest {
    @Schema(hidden = true)
    private Integer userId;

    private Integer optionId;
    private Boolean betType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Bet points must be greater than 0")
    private BigDecimal betPoints;

    @DecimalMin(value = "0.0", inclusive = false, message = "Bet rate must be greater than 0")
    private BigDecimal betRate;

    private BigDecimal rewardPoints;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Instant placedAt;

    @Schema(hidden = true)
    private Instant settledAt;
}

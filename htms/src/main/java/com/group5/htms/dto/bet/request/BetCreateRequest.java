package com.group5.htms.dto.bet.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class BetCreateRequest {
    @Schema(hidden = true)
    private Integer userId;

    @NotNull(message = "Option id is required")
    private Integer optionId;

    private Boolean betType;

    @NotNull(message = "Bet points is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bet points must be greater than 0")
    private BigDecimal betPoints;

    @NotNull(message = "Bet rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bet rate must be greater than 0")
    private BigDecimal betRate;

    private BigDecimal rewardPoints;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Instant placedAt;

    @Schema(hidden = true)
    private Instant settledAt;
}

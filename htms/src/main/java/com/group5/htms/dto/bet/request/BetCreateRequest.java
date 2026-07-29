package com.group5.htms.dto.bet.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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

    @Schema(hidden = true)
    @DecimalMin(value = "0.0", inclusive = false, message = "Bet rate must be greater than 0")
    private BigDecimal betRate;
}

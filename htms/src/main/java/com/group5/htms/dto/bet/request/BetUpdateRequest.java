package com.group5.htms.dto.bet.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class BetUpdateRequest {
    private Integer spectatorRoleId;
    private Integer assignmentId;

    @Size(max = 30, message = "Market type must not exceed 30 characters")
    private String marketType;

    private Integer predictedPosition;

    @DecimalMin(value = "0.0", inclusive = false, message = "Stake points must be greater than 0")
    private BigDecimal stakePoints;

    @DecimalMin(value = "0.0", inclusive = false, message = "Odds decimal must be greater than 0")
    private BigDecimal oddsDecimal;

    private BigDecimal potentialPayoutPoints;
    private BigDecimal payoutPoints;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Instant placedAt;
    private Instant settledAt;
    private Integer settledById;

    @Size(max = 20, message = "Settled type must not exceed 20 characters")
    private String settledType;
}

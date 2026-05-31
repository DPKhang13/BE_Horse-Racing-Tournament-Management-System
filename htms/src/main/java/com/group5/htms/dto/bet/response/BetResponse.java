package com.group5.htms.dto.bet.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class BetResponse {
    private Integer id;
    private Integer spectatorRoleId;
    private Integer assignmentId;
    private String marketType;
    private Integer predictedPosition;
    private BigDecimal stakePoints;
    private BigDecimal oddsDecimal;
    private BigDecimal potentialPayoutPoints;
    private BigDecimal payoutPoints;
    private String status;
    private Instant placedAt;
    private Instant settledAt;
    private Integer settledById;
    private String settledType;
}

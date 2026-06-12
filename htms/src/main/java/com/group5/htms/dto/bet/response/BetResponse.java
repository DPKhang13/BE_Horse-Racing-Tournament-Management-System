package com.group5.htms.dto.bet.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class BetResponse {
    private Integer id;
    private Integer userId;
    private Integer optionId;
    private Boolean betType;
    private BigDecimal betPoints;
    private BigDecimal betRate;
    private BigDecimal rewardPoints;
    private String status;
    private Instant placedAt;
    private Instant settledAt;
}

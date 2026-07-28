package com.group5.htms.dto.bet.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class BetListResponse {
    private Integer betId;
    private Integer userId;
    private Integer optionId;
    private Boolean betType;
    private BigDecimal betPoints;
    private BigDecimal betRate;
    private BigDecimal rewardPoints;
    private String status;
    private Instant placedAt;
    private Instant settledAt;
    private Integer raceId;
    private String raceName;
    private Integer raceNumber;
    private Instant scheduledAt;
    private Integer assignmentId;
    private Integer horseId;
    private String horseName;
    private Integer jockeyId;
    private String jockeyFullName;
    private String userFullName;
}

package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class RacePublishResponse {
    private Integer raceId;
    private String raceName;
    private String raceStatus;
    private Instant publishedAt;
    private Integer totalResults;
    private Integer winnerHorseId;
    private String winnerHorseName;
    private Integer winnerJockeyId;
    private String winnerJockeyName;
    private Integer totalBetsSettled;
    private BigDecimal totalRewardsPaid;
    private String message;
}

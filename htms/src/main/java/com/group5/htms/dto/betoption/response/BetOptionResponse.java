package com.group5.htms.dto.betoption.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class BetOptionResponse {
    private Integer optionId;
    private Integer raceId;
    private String raceName;
    private Integer raceNumber;
    private Instant scheduledAt;
    private Instant predictionClosesAt;
    private Integer assignmentId;
    private Integer horseId;
    private String horseName;
    private String horseAvatarUrl;
    private Integer gateNumber;
    private Integer jockeyId;
    private String jockeyName;
    private String jockeyFullName;
    private String jockeyAvatarUrl;
    private BigDecimal currentRate;
    private BigDecimal totalBetPoints;
    private Integer totalBetCount;
    private Instant updatedAt;
}

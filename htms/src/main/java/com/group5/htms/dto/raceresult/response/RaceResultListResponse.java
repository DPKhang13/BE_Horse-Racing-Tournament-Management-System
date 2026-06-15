package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class RaceResultListResponse {
    private Integer resultId;
    private Integer assignmentId;
    private Integer raceId;
    private Integer horseId;
    private Integer ownerId;
    private Integer finishPosition;
    private BigDecimal finishTimeSec;
    private Integer pointsAwarded;
    private Boolean isDisqualified;
    private String status;
    private Instant recordedAt;
    private Instant publishedAt;
    private String raceName;
    private Integer raceNumber;
    private Instant scheduledAt;
    private String horseName;
    private String horseAvatarUrl;
    private String ownerFullName;
    private String ownerStableName;
    private Integer jockeyId;
    private String jockeyFullName;
    private Integer gateNumber;
}

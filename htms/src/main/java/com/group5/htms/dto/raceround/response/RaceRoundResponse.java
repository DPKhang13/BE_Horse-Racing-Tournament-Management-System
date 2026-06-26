package com.group5.htms.dto.raceround.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class RaceRoundResponse {
    private Integer id;
    private Integer roundId;
    private Integer raceId;
    private Integer assignmentId;
    private Integer horseId;
    private Integer jockeyId;
    private Integer ownerId;
    private Integer roundNumber;
    private Integer position;
    private BigDecimal lapTimeSec;
    private Instant recordedAt;
    private String raceName;
    private Integer raceNumber;
    private Integer lapCount;
    private Instant scheduledAt;
    private Integer tournamentId;
    private String tournamentName;
    private String horseName;
    private String horseAvatarUrl;
    private String ownerFullName;
    private String jockeyFullName;
    private Integer gateNumber;
}

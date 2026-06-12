package com.group5.htms.dto.race.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class RaceResponse {
    private Integer id;
    private Integer tournamentId;
    private Integer scheduleId;
    private String name;
    private Integer raceNumber;
    private String rankGroup;
    private Integer lapCount;
    private Instant scheduledAt;
    private Instant predictionClosesAt;
    private Double distanceM;
    private String trackType;
    private Integer maxHorses;
    private Integer maxReferees;
    private String pointRuleNote;
    private String status;
}

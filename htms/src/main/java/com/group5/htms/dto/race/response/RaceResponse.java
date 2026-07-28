package com.group5.htms.dto.race.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

import com.group5.htms.dto.racepointrule.response.RacePointRuleResponse;

@Getter
@Builder
public class RaceResponse {
    private Integer id;
    private Integer raceId;
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
    private String status;
    private String tournamentName;
    private java.time.LocalDate raceDate;
    private Integer dayNumber;
    private String scheduleTitle;
    private String scheduleNote;
    private String location;
    private Long registeredHorseCount;
    private Long acceptedJockeyCount;
    private Long assignedRefereeCount;
    private List<RacePointRuleResponse> pointRules;
}

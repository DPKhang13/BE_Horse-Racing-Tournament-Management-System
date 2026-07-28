package com.group5.htms.dto.race.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class RaceListResponse {
    private Integer raceId;
    private Integer tournamentId;
    private Integer scheduleId;
    private String name;
    private Integer raceNumber;
    private String rankGroup;
    private Instant scheduledAt;
    private Instant predictionClosesAt;
    private Double distanceM;
    private String trackType;
    private Integer maxHorses;
    private String status;
    private String tournamentName;
    private LocalDate raceDate;
    private Integer dayNumber;
    private String scheduleTitle;
    private String location;
    private Long registeredHorseCount;
    private Long acceptedJockeyCount;
    private Long assignedRefereeCount;
}

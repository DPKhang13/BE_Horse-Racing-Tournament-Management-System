package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class TournamentRaceResultGroupResponse {
    private Integer raceId;
    private String raceName;
    private Integer raceNumber;
    private String raceStatus;
    private Instant scheduledAt;
    private Double distanceM;
    private String trackType;
    private List<RaceResultResponse> results;
}

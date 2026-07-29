package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class TournamentStandingResponse {
    private Integer rank;
    private Integer horseId;
    private String horseName;
    private String horseAvatarUrl;
    private Integer ownerId;
    private String ownerFullName;
    private String ownerStableName;
    private Integer totalPoints;
    private Integer winCount;
    private Integer bestFinishPosition;
    private BigDecimal bestPaceSecondsPerMeter;
}

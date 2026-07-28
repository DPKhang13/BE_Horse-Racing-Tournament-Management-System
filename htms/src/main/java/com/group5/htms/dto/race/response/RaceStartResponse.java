package com.group5.htms.dto.race.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class RaceStartResponse {
    private Integer raceId;
    private String raceName;
    private String previousStatus;
    private String status;
    private Instant scheduledAt;
    private Instant predictionClosesAt;
    private Boolean bettingClosed;
    private String message;
}

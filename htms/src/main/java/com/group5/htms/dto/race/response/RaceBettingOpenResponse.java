package com.group5.htms.dto.race.response;

import com.group5.htms.dto.betoption.response.BetOptionResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class RaceBettingOpenResponse {
    private Integer raceId;
    private String raceName;
    private String previousStatus;
    private String status;
    private Instant scheduledAt;
    private Instant predictionClosesAt;
    private List<BetOptionResponse> betOptions;
    private String message;
}

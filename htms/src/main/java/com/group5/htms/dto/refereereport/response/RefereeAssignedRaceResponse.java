package com.group5.htms.dto.refereereport.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class RefereeAssignedRaceResponse {
    private Integer raceId;
    private String raceName;
    private String status;
    private Instant scheduledAt;
    private Instant predictionClosesAt;
    private String refereeRole;
    private Integer assignmentId;
    private Instant assignedAt;
}

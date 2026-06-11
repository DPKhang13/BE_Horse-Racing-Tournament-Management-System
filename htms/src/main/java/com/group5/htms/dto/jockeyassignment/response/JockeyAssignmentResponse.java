package com.group5.htms.dto.jockeyassignment.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class JockeyAssignmentResponse {
    private Integer id;
    private Integer registrationId;
    private Integer raceId;
    private Integer jockeyId;
    private Integer gateNumber;
    private String status;
    private Instant invitedAt;
    private Instant respondedAt;
}

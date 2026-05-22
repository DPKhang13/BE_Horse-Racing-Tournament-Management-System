package com.group5.htms.dto.raceregistration.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class RaceRegistrationResponse {
    private Integer id;
    private Integer tournamentId;
    private Integer raceId;
    private Integer horseId;
    private Integer ownerRoleId;
    private Integer jockeyRoleId;
    private String status;
    private String ownerConfirmationStatus;
    private Instant ownerConfirmedAt;
    private Instant registeredAt;
    private Instant approvedAt;
    private Integer approvedById;
}

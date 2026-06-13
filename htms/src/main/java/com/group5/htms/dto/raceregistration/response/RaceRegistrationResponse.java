package com.group5.htms.dto.raceregistration.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class RaceRegistrationResponse {
    private Integer id;
    private Integer regId;
    private Integer tournamentId;
    private Integer raceId;
    private Integer horseId;
    private Integer ownerId;
    private Integer jockeyId;
    private String status;
    private String ownerConfirmationStatus;
    private Instant ownerConfirmedAt;
    private Instant registeredAt;
    private Instant approvedAt;
    private Integer approvedById;
    private String tournamentName;
    private String raceName;
    private Integer raceNumber;
    private Instant scheduledAt;
    private String horseName;
    private String horseAvatarUrl;
    private String ownerFullName;
    private String ownerStableName;
    private String jockeyFullName;
    private String jockeyStatus;
    private String approvedByFullName;
}

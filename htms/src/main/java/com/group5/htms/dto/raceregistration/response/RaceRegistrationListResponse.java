package com.group5.htms.dto.raceregistration.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class RaceRegistrationListResponse {
    private Integer regId;
    private Integer tournamentId;
    private Integer raceId;
    private Integer horseId;
    private Integer ownerId;
    private Integer jockeyId;
    private String status;
    private String ownerConfirmationStatus;
    private Instant registeredAt;
    private String tournamentName;
    private String raceName;
    private Integer raceNumber;
    private Instant scheduledAt;
    private String horseName;
    private String horseAvatarUrl;
    private String ownerFullName;
    private String ownerStableName;
    private String jockeyFullName;
}

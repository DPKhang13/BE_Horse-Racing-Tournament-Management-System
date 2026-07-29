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
    private Integer gateNumber;
    private String status;
    private String ownerConfirmationStatus;
    private Instant ownerConfirmedAt;
    private String chiefInspectionStatus;
    private Integer chiefInspectedById;
    private String chiefInspectedByFullName;
    private Instant chiefInspectedAt;
    private String chiefInspectionNote;
    private Instant registeredAt;
    private Instant approvedAt;
    private Integer approvedById;
    private Integer adminReviewedById;
    private String adminReviewedByFullName;
    private Instant adminReviewedAt;
    private String adminReviewNote;
    private String tournamentName;
    private String raceName;
    private String raceStatus;
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

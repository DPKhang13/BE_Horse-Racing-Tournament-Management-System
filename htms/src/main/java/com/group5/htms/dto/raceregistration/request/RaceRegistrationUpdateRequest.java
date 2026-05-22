package com.group5.htms.dto.raceregistration.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RaceRegistrationUpdateRequest {
    private Integer tournamentId;
    private Integer raceId;
    private Integer horseId;
    private Integer ownerRoleId;
    private Integer jockeyRoleId;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @Size(max = 20, message = "Owner confirmation status must not exceed 20 characters")
    private String ownerConfirmationStatus;

    private Instant ownerConfirmedAt;
    private Instant registeredAt;
    private Instant approvedAt;
    private Integer approvedById;
}

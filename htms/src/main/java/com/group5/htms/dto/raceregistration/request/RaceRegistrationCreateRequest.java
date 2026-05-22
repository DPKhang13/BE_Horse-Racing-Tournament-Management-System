package com.group5.htms.dto.raceregistration.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RaceRegistrationCreateRequest {
    @NotNull(message = "Tournament id is required")
    private Integer tournamentId;

    @NotNull(message = "Race id is required")
    private Integer raceId;

    @NotNull(message = "Horse id is required")
    private Integer horseId;

    @NotNull(message = "Owner role id is required")
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

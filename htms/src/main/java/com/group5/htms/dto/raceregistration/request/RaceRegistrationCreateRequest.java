package com.group5.htms.dto.raceregistration.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(hidden = true)
    private Integer ownerId;

    private Integer jockeyId;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @Size(max = 20, message = "Owner confirmation status must not exceed 20 characters")
    private String ownerConfirmationStatus;

    private Instant ownerConfirmedAt;
    private Instant registeredAt;
    @Schema(hidden = true)
    private Instant approvedAt;

    @Schema(hidden = true)
    private Integer approvedById;
}

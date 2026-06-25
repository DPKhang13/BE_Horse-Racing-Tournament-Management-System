package com.group5.htms.dto.raceregistration.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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
}

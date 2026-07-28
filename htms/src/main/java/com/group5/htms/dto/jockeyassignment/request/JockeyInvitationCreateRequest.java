package com.group5.htms.dto.jockeyassignment.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JockeyInvitationCreateRequest {
    @NotNull(message = "Registration id is required")
    private Integer registrationId;

    @NotNull(message = "Race id is required")
    private Integer raceId;

    @NotNull(message = "Jockey id is required")
    private Integer jockeyId;

    private Integer gateNumber;
}

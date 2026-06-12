package com.group5.htms.dto.jockeyassignment.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

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

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Instant invitedAt;
    private Instant respondedAt;
}

package com.group5.htms.dto.jockeyassignment.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class JockeyInvitationUpdateRequest {
    private Integer registrationId;
    private Integer raceId;
    private Integer jockeyRoleId;
    private Integer gateNumber;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Instant invitedAt;
    private Instant respondedAt;
}

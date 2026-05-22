package com.group5.htms.dto.jockeyassignment.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class JockeyInvitationResponseRequest {
    @NotBlank(message = "Status is required")
    private String status;

    private Instant respondedAt;
}

package com.group5.htms.dto.jockeyassignment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class JockeyInvitationResponseRequest {
    @NotBlank(message = "Status is required")
    private String status;

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    private Instant respondedAt;
}

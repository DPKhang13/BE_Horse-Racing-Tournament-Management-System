package com.group5.htms.dto.raceregistration.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RaceRegistrationApprovalRequest {
    @NotBlank(message = "Status is required")
    private String status;

    private Integer approvedById;
    private Instant approvedAt;
}

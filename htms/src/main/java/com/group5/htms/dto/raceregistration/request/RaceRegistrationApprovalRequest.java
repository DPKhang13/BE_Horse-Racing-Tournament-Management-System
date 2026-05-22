package com.group5.htms.dto.raceregistration.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RaceRegistrationApprovalRequest {
    @NotBlank(message = "Status is required")
    private String status;

    @Schema(hidden = true)
    private Integer approvedById;
    private Instant approvedAt;
}

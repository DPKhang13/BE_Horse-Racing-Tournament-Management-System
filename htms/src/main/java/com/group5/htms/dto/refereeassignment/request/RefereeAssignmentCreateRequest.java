package com.group5.htms.dto.refereeassignment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefereeAssignmentCreateRequest {

    @Schema(hidden = true)
    private Integer raceId;

    @NotNull(message = "Referee id is required")
    private Integer refereeId;

    @NotBlank(message = "Referee role is required")
    @Size(max = 50, message = "Referee role must not exceed 50 characters")
    private String refereeRole;
}

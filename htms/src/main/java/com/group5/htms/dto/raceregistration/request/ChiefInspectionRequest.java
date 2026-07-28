package com.group5.htms.dto.raceregistration.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChiefInspectionRequest {
    @NotBlank(message = "Inspection status is required")
    @Size(max = 20, message = "Inspection status must not exceed 20 characters")
    private String status;

    @NotBlank(message = "Inspection note is required")
    @Size(max = 1000, message = "Inspection note must not exceed 1000 characters")
    private String note;
}

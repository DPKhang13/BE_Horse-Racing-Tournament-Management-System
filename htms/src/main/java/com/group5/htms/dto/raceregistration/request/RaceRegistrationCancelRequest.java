package com.group5.htms.dto.raceregistration.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaceRegistrationCancelRequest {
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
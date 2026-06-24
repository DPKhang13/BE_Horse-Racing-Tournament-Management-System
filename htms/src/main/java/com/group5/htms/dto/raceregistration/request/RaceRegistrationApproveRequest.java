package com.group5.htms.dto.raceregistration.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaceRegistrationApproveRequest {
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}

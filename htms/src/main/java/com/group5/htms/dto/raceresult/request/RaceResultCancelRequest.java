package com.group5.htms.dto.raceresult.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaceResultCancelRequest {
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}

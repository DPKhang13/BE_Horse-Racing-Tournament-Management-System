package com.group5.htms.dto.raceresult.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RaceResultPublishRequest {
    @NotBlank(message = "Status is required")
    private String status;

    private Instant publishedAt;
}

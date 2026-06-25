package com.group5.htms.dto.tournament.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRegistrationRequest {
    private Instant registrationOpenAt;

    @NotNull(message = "Registration close time is required")
    private Instant registrationCloseAt;
}

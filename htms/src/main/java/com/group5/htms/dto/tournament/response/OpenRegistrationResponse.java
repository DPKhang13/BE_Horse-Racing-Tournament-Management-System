package com.group5.htms.dto.tournament.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OpenRegistrationResponse {
    private Integer tournamentId;
    private String tournamentName;
    private String status;
    private Instant registrationOpenAt;
    private Instant registrationCloseAt;
    private Integer totalSchedules;
    private Integer totalRaces;
}

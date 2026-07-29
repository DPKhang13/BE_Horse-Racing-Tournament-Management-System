package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class TournamentResultResponse {
    private Integer tournamentId;
    private String tournamentName;
    private String status;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TournamentRaceResultGroupResponse> races;
    private List<TournamentStandingResponse> standings;
}

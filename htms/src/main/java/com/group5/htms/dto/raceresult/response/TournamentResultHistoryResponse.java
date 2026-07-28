package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@Getter
public class TournamentResultHistoryResponse {
    private Integer tournamentId;
    private String tournamentName;
    private String status;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalRaces;
    private Long racesWithResults;
    private Long draftResultCount;
    private Long confirmedResultCount;
    private Long publishedResultCount;
    private Long cancelledResultCount;
    private Instant latestPublishedAt;
}

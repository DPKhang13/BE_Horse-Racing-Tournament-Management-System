package com.group5.htms.dto.tournament.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CloseRegistrationResponse {
    private Integer tournamentId;
    private String tournamentName;
    private String status;
    private Integer rejectedPendingRegistrations;
    private Integer cancelledUnconfirmedRegistrations;
    private Integer closedRaceCount;
    private Integer readyRaceCount;
    private String message;
}

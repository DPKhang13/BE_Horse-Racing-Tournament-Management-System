package com.group5.htms.dto.schedule.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentScheduleResponse {

    private Integer scheduleId;

    private Integer tournamentId;

    private String tournamentName;

    private LocalDate raceDate;

    private Integer dayNumber;

    private String title;

    private String note;
}

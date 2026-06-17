package com.group5.htms.service;

import com.group5.htms.dto.race.request.RaceCreateRequest;
import com.group5.htms.dto.race.request.RaceUpdateRequest;
import com.group5.htms.dto.race.response.RaceListResponse;
import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.dto.schedule.request.TournamentScheduleCreateRequest;
import com.group5.htms.dto.schedule.request.TournamentScheduleUpdateRequest;
import com.group5.htms.dto.schedule.response.TournamentScheduleResponse;

import java.util.List;

public interface RaceService {
    TournamentScheduleResponse createSchedule(
            Integer tournamentId,
            TournamentScheduleCreateRequest request
    );

    RaceResponse createRace(Integer scheduleId, RaceCreateRequest request);

    List<TournamentScheduleResponse> getSchedulesByTournament(Integer tournamentId);

    TournamentScheduleResponse getScheduleById(Integer scheduleId);

    TournamentScheduleResponse updateSchedule(
            Integer scheduleId,
            TournamentScheduleUpdateRequest request
    );

    void deleteSchedule(Integer scheduleId);

    RaceResponse getRaceById(Integer raceId);

    RaceResponse updateRace(Integer raceId, RaceUpdateRequest request);

    void cancelRace(Integer raceId);

    List<RaceListResponse> getRacesByTournament(Integer tournamentId, String status);
}

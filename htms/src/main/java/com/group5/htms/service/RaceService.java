package com.group5.htms.service;

import com.group5.htms.dto.race.response.RaceListResponse;

import java.util.List;

public interface RaceService {
    List<RaceListResponse> getRacesByTournament(Integer tournamentId, String status);
}

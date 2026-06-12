package com.group5.htms.service;

import com.group5.htms.dto.race.response.RaceResponse;

import java.util.List;

public interface RaceService {
    List<RaceResponse> getRacesByTournament(Integer tournamentId, String status);
}

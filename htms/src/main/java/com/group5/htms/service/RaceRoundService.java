package com.group5.htms.service;

import com.group5.htms.dto.raceround.request.RaceRoundCreateRequest;
import com.group5.htms.dto.raceround.request.RaceRoundUpdateRequest;
import com.group5.htms.dto.raceround.response.RaceRoundResponse;

import java.util.List;

public interface RaceRoundService {
    List<RaceRoundResponse> getAllRounds(Integer raceId, Integer assignmentId);

    RaceRoundResponse getRoundById(Integer id);

    RaceRoundResponse createRound(RaceRoundCreateRequest request);

    RaceRoundResponse updateRound(Integer id, RaceRoundUpdateRequest request);
}

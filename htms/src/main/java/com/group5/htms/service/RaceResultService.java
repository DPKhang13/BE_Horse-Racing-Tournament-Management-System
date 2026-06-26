package com.group5.htms.service;

import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;

import java.util.List;

public interface RaceResultService {
    List<RaceResultListResponse> getAllResults();

    RaceResultResponse getResultById(Integer id);

    RaceResultResponse createResult(RaceResultCreateRequest request);

    RaceResultResponse updateResult(Integer id, RaceResultUpdateRequest request);

    RaceResultResponse publishResult(Integer id, RaceResultPublishRequest request);

    List<RaceResultListResponse> calculateResultsFromRounds(Integer raceId);
}

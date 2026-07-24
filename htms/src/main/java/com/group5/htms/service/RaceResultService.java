package com.group5.htms.service;

import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultCancelRequest;
import com.group5.htms.dto.raceresult.request.RaceResultDraftRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import com.group5.htms.dto.raceresult.response.RacePublishResponse;
import com.group5.htms.dto.raceresult.response.RaceResultDraftResponse;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.dto.raceresult.response.TournamentResultHistoryResponse;
import com.group5.htms.dto.raceresult.response.TournamentResultResponse;

import java.util.List;

public interface RaceResultService {
    List<RaceResultListResponse> getAllResults();

    List<RaceResultResponse> getResultById(Integer id);

    List<RaceResultListResponse> getResultsByRace(Integer raceId);

    RaceResultResponse createResult(RaceResultCreateRequest request);

    RaceResultResponse updateResult(Integer id, RaceResultUpdateRequest request);

    RaceResultResponse publishResult(Integer id, RaceResultPublishRequest request);

    List<RaceResultListResponse> calculateResultsFromRounds(Integer raceId);

    RaceResultDraftResponse createDraft(Integer raceId, RaceResultDraftRequest request);

    RaceResultDraftResponse replaceDraft(Integer raceId, RaceResultDraftRequest request);

    RaceResultDraftResponse replaceDraftByAdmin(Integer raceId, RaceResultDraftRequest request);

    RaceResultDraftResponse getDraft(Integer raceId);

    List<RaceResultResponse> getAdminResults(Integer raceId);

    List<RaceResultResponse> confirmResults(Integer raceId);

    void cancelResults(Integer raceId, RaceResultCancelRequest request);

    RacePublishResponse publishRaceResults(Integer raceId);

    List<RaceResultResponse> getPublicResults(Integer raceId);

    TournamentResultResponse getPublicTournamentResults(Integer tournamentId);

    TournamentResultResponse getAdminTournamentResults(Integer tournamentId, String status);

    List<TournamentResultHistoryResponse> getAdminTournamentResultHistory(String status, String resultStatus);
}


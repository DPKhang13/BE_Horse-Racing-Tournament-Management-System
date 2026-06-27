package com.group5.htms.service;

import com.group5.htms.dto.bet.request.BetCheckRequest;
import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetListResponse;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.dashboard.response.PredictionRaceResponse;
import com.group5.htms.dto.dashboard.response.SpectatorDashboardResponse;

import java.util.List;

public interface BetService {
    List<BetListResponse> getAllBets();

    BetResponse getBetById(Integer id);

    BetResponse createBet(BetCreateRequest request);

    BetResponse updateBet(Integer id, BetUpdateRequest request);

    BetResponse checkBet(Integer id, BetCheckRequest request);

    SpectatorDashboardResponse getSpectatorDashboard();

    List<PredictionRaceResponse> getOpenPredictionRaces();
}

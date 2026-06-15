package com.group5.htms.service;

import com.group5.htms.dto.bet.request.BetCheckRequest;
import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetListResponse;
import com.group5.htms.dto.bet.response.BetResponse;

import java.util.List;

public interface BetService {
    List<BetListResponse> getAllBets();

    BetResponse getBetById(Integer id);

    BetResponse createBet(BetCreateRequest request);

    BetResponse updateBet(Integer id, BetUpdateRequest request);

    BetResponse checkBet(Integer id, BetCheckRequest request);

    void deleteBet(Integer id);
}

package com.group5.htms.service;

import com.group5.htms.dto.prize.request.PrizeCreateRequest;
import com.group5.htms.dto.prize.response.PrizeResponse;

import java.util.List;

public interface PrizeService {

    List<PrizeResponse> createPrizes(
            Integer tournamentId,
            PrizeCreateRequest request
    );

    List<PrizeResponse> getPrizesByTournament(Integer tournamentId);
}
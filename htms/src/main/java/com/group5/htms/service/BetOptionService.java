package com.group5.htms.service;

import com.group5.htms.dto.betoption.request.BetOptionRateUpdateRequest;
import com.group5.htms.dto.betoption.response.BetOptionResponse;

import java.util.List;

public interface BetOptionService {
    List<BetOptionResponse> generateBetOptionsForRace(Integer raceId);

    BetOptionResponse updateCurrentRate(Integer optionId, BetOptionRateUpdateRequest request);

    List<BetOptionResponse> getAllBetOptions();

    List<BetOptionResponse> getBetOptionsByRace(Integer raceId);

    BetOptionResponse getBetOptionById(Integer id);
}

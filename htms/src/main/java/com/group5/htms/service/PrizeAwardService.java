package com.group5.htms.service;

import com.group5.htms.dto.prize.response.PrizeAwardResponse;

import java.util.List;

public interface PrizeAwardService {

    List<PrizeAwardResponse> awardTournamentPrizes(Integer tournamentId);

    List<PrizeAwardResponse> getTournamentPrizeAwards(Integer tournamentId);

    PrizeAwardResponse markPrizeAwarded(Integer tournamentId, Integer awardId);
}

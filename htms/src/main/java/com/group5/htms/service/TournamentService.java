package com.group5.htms.service;

import com.group5.htms.dto.tournament.request.TournamentCreateRequest;
import com.group5.htms.dto.tournament.request.TournamentUpdateRequest;
import com.group5.htms.dto.tournament.response.TournamentResponse;

import java.util.List;

public interface TournamentService {

    TournamentResponse createTournament(TournamentCreateRequest request);

    TournamentResponse updateTournament(Integer tournamentId, TournamentUpdateRequest request);

    TournamentResponse getTournamentById(Integer tournamentId);

    List<TournamentResponse> getAllTournaments(String status);

    //void deleteTournament(Integer tournamentId);

    TournamentResponse cancelTournament(Integer tournamentId);

}
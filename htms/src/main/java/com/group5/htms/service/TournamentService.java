package com.group5.htms.service;

import com.group5.htms.dto.tournament.request.OpenRegistrationRequest;
import com.group5.htms.dto.tournament.request.CloseRegistrationRequest;
import com.group5.htms.dto.tournament.request.TournamentCreateRequest;
import com.group5.htms.dto.tournament.request.TournamentUpdateRequest;
import com.group5.htms.dto.tournament.response.CloseRegistrationResponse;
import com.group5.htms.dto.tournament.response.GlobalTournamentCountResponse;
import com.group5.htms.dto.tournament.response.OpenRegistrationResponse;
import com.group5.htms.dto.tournament.response.TournamentDetailResponse;
import com.group5.htms.dto.tournament.response.TournamentResponse;
import com.group5.htms.dto.tournament.response.TournamentSummaryResponse;

import java.util.List;

public interface TournamentService {
    GlobalTournamentCountResponse getGlobalTournamentCount();

    TournamentResponse createTournament(TournamentCreateRequest request);

    TournamentResponse updateTournament(Integer tournamentId, TournamentUpdateRequest request);

    TournamentDetailResponse getTournamentById(Integer tournamentId);

    List<TournamentSummaryResponse> getAllTournaments(String status);

    TournamentResponse cancelTournament(Integer tournamentId);

    OpenRegistrationResponse openRegistration(Integer tournamentId, OpenRegistrationRequest request);

    CloseRegistrationResponse closeRegistration(Integer tournamentId, CloseRegistrationRequest request);

}

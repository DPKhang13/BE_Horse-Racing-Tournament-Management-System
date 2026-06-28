package com.group5.htms.controller;

import com.group5.htms.dto.tournament.request.CloseRegistrationRequest;
import com.group5.htms.dto.tournament.request.OpenRegistrationRequest;
import com.group5.htms.dto.tournament.response.CloseRegistrationResponse;
import com.group5.htms.dto.tournament.response.OpenRegistrationResponse;
import com.group5.htms.dto.tournament.response.TournamentResponse;
import com.group5.htms.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/tournaments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTournamentController {
    private final TournamentService tournamentService;

    @PatchMapping("/{tournamentId}/open-registration")
    public ResponseEntity<OpenRegistrationResponse> openRegistration(
            @PathVariable Integer tournamentId,
            @Valid @RequestBody OpenRegistrationRequest request
    ) {
        return ResponseEntity.ok(tournamentService.openRegistration(tournamentId, request));
    }

    @PatchMapping("/{tournamentId}/close-registration")
    public ResponseEntity<CloseRegistrationResponse> closeRegistration(
            @PathVariable Integer tournamentId,
            @Valid @RequestBody(required = false) CloseRegistrationRequest request
    ) {
        return ResponseEntity.ok(tournamentService.closeRegistration(tournamentId, request));
    }

    @PatchMapping("/{tournamentId}/start")
    public ResponseEntity<TournamentResponse> startTournament(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(tournamentService.startTournament(tournamentId));
    }

    @PatchMapping("/{tournamentId}/complete")
    public ResponseEntity<TournamentResponse> completeTournament(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(tournamentService.completeTournament(tournamentId));
    }
}

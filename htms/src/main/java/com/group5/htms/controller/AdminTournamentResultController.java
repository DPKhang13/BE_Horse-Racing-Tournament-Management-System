package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.response.TournamentResultResponse;
import com.group5.htms.service.RaceResultService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/tournaments/{tournamentId}/results")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTournamentResultController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Admin get tournament results", description = "Admin xem ket qua tournament, co the filter theo status draft/confirmed/published/cancelled.")
    @GetMapping
    public ResponseEntity<TournamentResultResponse> getAdminTournamentResults(
            @PathVariable Integer tournamentId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(raceResultService.getAdminTournamentResults(tournamentId, status));
    }
}

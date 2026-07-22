package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.response.TournamentResultResponse;
import com.group5.htms.service.RaceResultService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tournaments/{tournamentId}/results")
public class PublicTournamentResultController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Get public tournament results", description = "Public xem ket qua da published cua tournament, gom theo race va standings tong.")
    @GetMapping("/public")
    public ResponseEntity<TournamentResultResponse> getPublicTournamentResults(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(raceResultService.getPublicTournamentResults(tournamentId));
    }
}

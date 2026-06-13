package com.group5.htms.controller;

import com.group5.htms.dto.tournament.request.TournamentCreateRequest;
import com.group5.htms.dto.tournament.request.TournamentUpdateRequest;
import com.group5.htms.dto.tournament.response.TournamentDetailResponse;
import com.group5.htms.dto.tournament.response.TournamentResponse;
import com.group5.htms.dto.tournament.response.TournamentSummaryResponse;
import com.group5.htms.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponse> createTournament(
            @Valid @RequestBody TournamentCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tournamentService.createTournament(request));
    }

    @PutMapping("/update/{tournamentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponse> updateTournament(
            @PathVariable Integer tournamentId,
            @Valid @RequestBody TournamentUpdateRequest request
    ) {
        return ResponseEntity.ok(
                tournamentService.updateTournament(tournamentId, request)
        );
    }

    @GetMapping("/getId/{tournamentId}")
    public ResponseEntity<TournamentDetailResponse> getTournamentById(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(
                tournamentService.getTournamentById(tournamentId)
        );
    }

    @GetMapping("getAll")
    public ResponseEntity<List<TournamentSummaryResponse>> getAllTournaments(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                tournamentService.getAllTournaments(status)
        );
    }

//    @DeleteMapping("/{tournamentId}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Map<String, String>> deleteTournament(
//            @PathVariable Integer tournamentId
//    ) {
//        tournamentService.deleteTournament(tournamentId);
//
//        return ResponseEntity.ok(
//                Map.of("message", "Tournament deleted successfully")
//        );
//    }

    @PatchMapping("/cancel/{tournamentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TournamentResponse> cancelTournament(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(
                tournamentService.cancelTournament(tournamentId)
        );
    }
}

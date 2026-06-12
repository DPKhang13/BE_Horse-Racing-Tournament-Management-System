package com.group5.htms.controller;

import com.group5.htms.dto.prize.request.PrizeCreateRequest;
import com.group5.htms.dto.prize.response.PrizeResponse;
import com.group5.htms.service.PrizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/tournaments")
@PreAuthorize("hasRole('ADMIN')")
public class PrizeController {

    private final PrizeService prizeService;

    @PostMapping("/create/{tournamentId}")
    public ResponseEntity<List<PrizeResponse>> createPrizes(
            @PathVariable Integer tournamentId,
            @Valid @RequestBody PrizeCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(prizeService.createPrizes(tournamentId, request));
    }

    @GetMapping("/getId/{tournamentId}")
    public ResponseEntity<List<PrizeResponse>> getPrizesByTournament(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(
                prizeService.getPrizesByTournament(tournamentId)
        );
    }
}
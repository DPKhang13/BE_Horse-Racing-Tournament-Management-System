package com.group5.htms.controller;

import com.group5.htms.dto.prize.request.PrizeCreateRequest;
import com.group5.htms.dto.prize.request.PrizeUpdateRequest;
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
public class PrizeController {

    private final PrizeService prizeService;

    @PostMapping("/{tournamentId}/create-prizes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrizeResponse>> createPrizes(
            @PathVariable Integer tournamentId,
            @Valid @RequestBody PrizeCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(prizeService.createPrizes(tournamentId, request));
    }

    @GetMapping("/{tournamentId}/get-prizes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SPECTATOR')")
    public ResponseEntity<List<PrizeResponse>> getPrizesByTournament(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(
                prizeService.getPrizesByTournament(tournamentId)
        );
    }

    @GetMapping("/{tournamentId}/get-prize/{prizeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrizeResponse> getPrizeById(
            @PathVariable Integer tournamentId,
            @PathVariable Integer prizeId
    ) {
        return ResponseEntity.ok(
                prizeService.getPrizeById(tournamentId, prizeId)
        );
    }

    @PutMapping("/{tournamentId}/update-prize/{prizeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrizeResponse> updatePrize(
            @PathVariable Integer tournamentId,
            @PathVariable Integer prizeId,
            @Valid @RequestBody PrizeUpdateRequest request
    ) {
        return ResponseEntity.ok(
                prizeService.updatePrize(tournamentId, prizeId, request)
        );
    }
}

package com.group5.htms.controller;

import com.group5.htms.dto.prize.request.PrizeCreateRequest;
import com.group5.htms.dto.prize.request.PrizeUpdateRequest;
import com.group5.htms.dto.prize.response.PrizeAwardResponse;
import com.group5.htms.dto.prize.response.PrizeResponse;
import com.group5.htms.service.PrizeAwardService;
import com.group5.htms.service.PrizeService;
import io.swagger.v3.oas.annotations.Operation;
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
    private final PrizeAwardService prizeAwardService;

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

    @PatchMapping("/{tournamentId}/award-prizes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Announce tournament prize recipients", description = "Xac dinh top 1, 2, 3 va lap danh sach trao thuong ngoai he thong. Khong cong tien vao vi.")
    public ResponseEntity<List<PrizeAwardResponse>> awardPrizes(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(
                prizeAwardService.awardTournamentPrizes(tournamentId)
        );
    }

    @PatchMapping("/{tournamentId}/prize-awards/{awardId}/mark-awarded")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark an external tournament prize as awarded")
    public ResponseEntity<PrizeAwardResponse> markPrizeAwarded(
            @PathVariable Integer tournamentId,
            @PathVariable Integer awardId
    ) {
        return ResponseEntity.ok(prizeAwardService.markPrizeAwarded(tournamentId, awardId));
    }

    @GetMapping("/{tournamentId}/prize-awards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrizeAwardResponse>> getPrizeAwards(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(
                prizeAwardService.getTournamentPrizeAwards(tournamentId)
        );
    }
}

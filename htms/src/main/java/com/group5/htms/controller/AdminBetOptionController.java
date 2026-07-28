package com.group5.htms.controller;

import com.group5.htms.dto.betoption.request.BetOptionRateUpdateRequest;
import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.service.BetOptionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bet-options")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBetOptionController {
    private final BetOptionService betOptionService;

    @Operation(summary = "Generate bet options by race", description = "Admin trigger tao bet options tu accepted jockey assignments cua race.")
    @PostMapping("/generate-by-race/{raceId}")
    public ResponseEntity<List<BetOptionResponse>> generateBetOptionsForRace(
            @PathVariable Integer raceId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(betOptionService.generateBetOptionsForRace(raceId));
    }

    @Operation(summary = "Update bet option rate", description = "Admin chỉ cập nhật currentRate của bet option.")
    @PutMapping("/{optionId}/rate")
    public ResponseEntity<BetOptionResponse> updateCurrentRate(
            @PathVariable Integer optionId,
            @Valid @RequestBody BetOptionRateUpdateRequest request
    ) {
        return ResponseEntity.ok(
                betOptionService.updateCurrentRate(optionId, request)
        );
    }
}

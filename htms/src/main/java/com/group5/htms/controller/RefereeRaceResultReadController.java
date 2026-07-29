package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.response.RaceResultDraftResponse;
import com.group5.htms.service.RaceResultService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/referee/races/{raceId}/results")
@PreAuthorize("hasRole('RACE_REFEREE')")
public class RefereeRaceResultReadController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Get race results", description = "An assigned referee can view the current race result draft.")
    @GetMapping("/draft")
    public ResponseEntity<RaceResultDraftResponse> getDraft(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceResultService.getDraft(raceId));
    }
}

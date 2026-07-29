package com.group5.htms.controller;

import com.group5.htms.dto.race.request.RaceStartRequest;
import com.group5.htms.dto.race.response.RaceStartResponse;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentListResponse;
import com.group5.htms.service.RaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/referee/chief/races")
@PreAuthorize("hasRole('RACE_REFEREE')")
public class ChiefRefereeRaceController {
    private final RaceService raceService;

    @Operation(summary = "Get race participants", description = "Chief referee views approved participants for an assigned race.")
    @GetMapping("/{raceId}/participants")
    public ResponseEntity<List<JockeyAssignmentListResponse>> getParticipants(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceService.getApprovedParticipantsForChief(raceId));
    }

    @Operation(summary = "Start race", description = "Chief referee starts an assigned race and closes betting when needed.")
    @PatchMapping("/{raceId}/start")
    public ResponseEntity<RaceStartResponse> startRace(
            @PathVariable Integer raceId,
            @Valid @RequestBody(required = false) RaceStartRequest request
    ) {
        return ResponseEntity.ok(raceService.startRace(raceId, request));
    }
}

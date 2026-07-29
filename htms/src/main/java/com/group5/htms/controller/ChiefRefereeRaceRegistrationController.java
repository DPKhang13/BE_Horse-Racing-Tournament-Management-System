package com.group5.htms.controller;

import com.group5.htms.dto.raceregistration.request.ChiefInspectionRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationListResponse;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.service.RaceRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/referee/chief/races/{raceId}/registrations")
@PreAuthorize("hasRole('RACE_REFEREE')")
public class ChiefRefereeRaceRegistrationController {
    private final RaceRegistrationService raceRegistrationService;

    @Operation(summary = "Get chief inspection registrations")
    @GetMapping("/inspection")
    public ResponseEntity<List<RaceRegistrationListResponse>> getInspectionRegistrations(
            @PathVariable Integer raceId
    ) {
        return ResponseEntity.ok(raceRegistrationService.getChiefInspectionRegistrations(raceId));
    }

    @Operation(summary = "Approve or reject horse inspection")
    @PatchMapping("/{registrationId}/inspection")
    public ResponseEntity<RaceRegistrationResponse> inspectRegistration(
            @PathVariable Integer raceId,
            @PathVariable Integer registrationId,
            @Valid @RequestBody ChiefInspectionRequest request
    ) {
        return ResponseEntity.ok(raceRegistrationService.inspectRegistration(raceId, registrationId, request));
    }
}

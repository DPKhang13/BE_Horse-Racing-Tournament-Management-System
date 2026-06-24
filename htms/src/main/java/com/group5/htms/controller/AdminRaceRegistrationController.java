package com.group5.htms.controller;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationApproveRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationRejectRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.service.RaceRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/race-registrations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRaceRegistrationController {
    private final RaceRegistrationService raceRegistrationService;

    @PatchMapping("/{registrationId}/approve")
    public ResponseEntity<RaceRegistrationResponse> approveRegistration(
            @PathVariable Integer registrationId,
            @Valid @RequestBody(required = false) RaceRegistrationApproveRequest request
    ) {
        return ResponseEntity.ok(
                raceRegistrationService.approveRegistration(
                        registrationId,
                        request == null ? new RaceRegistrationApproveRequest() : request
                )
        );
    }

    @PatchMapping("/{registrationId}/reject")
    public ResponseEntity<RaceRegistrationResponse> rejectRegistration(
            @PathVariable Integer registrationId,
            @Valid @RequestBody(required = false) RaceRegistrationRejectRequest request
    ) {
        return ResponseEntity.ok(
                raceRegistrationService.rejectRegistration(
                        registrationId,
                        request == null ? new RaceRegistrationRejectRequest() : request
                )
        );
    }
}

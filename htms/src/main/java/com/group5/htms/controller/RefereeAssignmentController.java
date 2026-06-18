package com.group5.htms.controller;

import com.group5.htms.dto.refereeassignment.request.RefereeAssignmentCreateRequest;
import com.group5.htms.dto.refereeassignment.response.RefereeAssignmentResponse;
import com.group5.htms.service.RefereeAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/races")
@PreAuthorize("hasRole('ADMIN')")
public class RefereeAssignmentController {

    private final RefereeAssignmentService refereeAssignmentService;

    @PostMapping("/{raceId}/create-referee-assignment")
    public ResponseEntity<RefereeAssignmentResponse> assignRefereeToRace(
            @PathVariable Integer raceId,
            @Valid @RequestBody RefereeAssignmentCreateRequest request
    ) {
        request.setRaceId(raceId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(refereeAssignmentService.assignRefereeToRace(raceId, request));
    }

    @GetMapping("/{raceId}/get-referee-assignment-list")
    public ResponseEntity<List<RefereeAssignmentResponse>> getRefereesByRace(
            @PathVariable Integer raceId
    ) {
        return ResponseEntity.ok(
                refereeAssignmentService.getRefereesByRace(raceId)
        );
    }
}

package com.group5.htms.controller;

import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;
import com.group5.htms.service.JockeyAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner/jockey-assignments")
@PreAuthorize("hasAnyRole('HORSE_OWNER', 'ADMIN')")
public class HorseJockeyAssignmentController {
    private final JockeyAssignmentService jockeyAssignmentService;

    @PatchMapping("/{assignmentId}/cancel")
    public ResponseEntity<JockeyAssignmentResponse> cancelInvitation(
            @PathVariable Integer assignmentId
    ) {
        return ResponseEntity.ok(jockeyAssignmentService.cancelInvitation(assignmentId));
    }

    @PatchMapping("/{assignmentId}/confirm")
    public ResponseEntity<JockeyAssignmentResponse> confirmAssignment(
            @PathVariable Integer assignmentId
    ) {
        return ResponseEntity.ok(jockeyAssignmentService.confirmAssignment(assignmentId));
    }
}

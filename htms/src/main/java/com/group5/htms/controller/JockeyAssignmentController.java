package com.group5.htms.controller;

import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationResponseRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;
import com.group5.htms.service.JockeyAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jockey-assignments")
@RequiredArgsConstructor
public class JockeyAssignmentController {
    private final JockeyAssignmentService jockeyAssignmentService;

    @Operation(summary = "Get all jockey assignments", description = "Lấy danh sách tất cả lời mời/assignment jockey.")
    @GetMapping("/get-all")
    public ResponseEntity<List<JockeyAssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(jockeyAssignmentService.getAllAssignments());
    }

    @Operation(summary = "Get jockey assignment by id", description = "Lấy thông tin assignment jockey theo id.")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<JockeyAssignmentResponse> getAssignmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(jockeyAssignmentService.getAssignmentById(id));
    }

    @Operation(summary = "Create jockey invitation", description = "Tạo lời mời jockey cho một đăng ký race.")
    @PostMapping("/create-invitation")
    @PreAuthorize("hasRole('HORSE_OWNER')")
    public ResponseEntity<JockeyAssignmentResponse> createInvitation(
            @Valid @RequestBody JockeyInvitationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jockeyAssignmentService.createInvitation(request));
    }

    @Operation(summary = "Update jockey invitation", description = "Cập nhật thông tin lời mời/assignment jockey.")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('HORSE_OWNER')")
    public ResponseEntity<JockeyAssignmentResponse> updateInvitation(
            @PathVariable Integer id,
            @Valid @RequestBody JockeyInvitationUpdateRequest request
    ) {
        return ResponseEntity.ok(jockeyAssignmentService.updateInvitation(id, request));
    }

    @Operation(summary = "Respond jockey invitation", description = "Cho phép jockey accept hoặc reject lời mời.")
    @PutMapping("/respond/{id}")
    @PreAuthorize("hasRole('JOCKEY')")
    public ResponseEntity<JockeyAssignmentResponse> respondInvitation(
            @PathVariable Integer id,
            @Valid @RequestBody JockeyInvitationResponseRequest request
    ) {
        return ResponseEntity.ok(jockeyAssignmentService.respondInvitation(id, request));
    }

    @Operation(summary = "Delete jockey assignment", description = "Xóa assignment jockey theo id.")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('HORSE_OWNER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Integer id) {
        jockeyAssignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}

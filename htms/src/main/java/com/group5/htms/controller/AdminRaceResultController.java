package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.request.RaceResultCancelRequest;
import com.group5.htms.dto.raceresult.response.RacePublishResponse;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.service.RaceResultService;
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
@RequestMapping("/api/v1/admin/races/{raceId}/results")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRaceResultController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Admin get race results", description = "Admin xem result cua race.")
    @GetMapping("/get")
    public ResponseEntity<List<RaceResultResponse>> getAdminResults(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceResultService.getAdminResults(raceId));
    }

    @Operation(summary = "Admin confirm race results", description = "Admin validate va tinh pointsAwarded cho result.")
    @PatchMapping("/confirm")
    public ResponseEntity<List<RaceResultResponse>> confirmResults(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceResultService.confirmResults(raceId));
    }

    @Operation(summary = "Admin cancel race results", description = "Admin huy result truoc khi publish, khong refund bet.")
    @PatchMapping("/cancel")
    public ResponseEntity<Void> cancelResults(
            @PathVariable Integer raceId,
            @Valid @RequestBody(required = false) RaceResultCancelRequest request
    ) {
        raceResultService.cancelResults(raceId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Admin publish race results", description = "Admin publish result, cong diem, settle bet va complete race.")
    @PatchMapping("/publish")
    public ResponseEntity<RacePublishResponse> publishRaceResults(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceResultService.publishRaceResults(raceId));
    }
}

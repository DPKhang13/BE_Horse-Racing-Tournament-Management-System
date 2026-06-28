package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.request.RaceResultDraftRequest;
import com.group5.htms.dto.raceresult.response.RaceResultDraftResponse;
import com.group5.htms.service.RaceResultService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/referee/races/{raceId}/results/draft")
@PreAuthorize("hasRole('RACE_REFEREE')")
public class RefereeRaceResultController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Create race result draft", description = "Chief/main referee tạo draft kết quả theo race.")
    @PostMapping("/create")
    public ResponseEntity<RaceResultDraftResponse> createDraft(
            @PathVariable Integer raceId,
            @Valid @RequestBody RaceResultDraftRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceResultService.createDraft(raceId, request));
    }

    @Operation(summary = "Replace race result draft", description = "Chief/main referee thay thế toàn bộ draft kết quar theo race.")
    @PutMapping("/update")
    public ResponseEntity<RaceResultDraftResponse> replaceDraft(
            @PathVariable Integer raceId,
            @Valid @RequestBody RaceResultDraftRequest request
    ) {
        return ResponseEntity.ok(raceResultService.replaceDraft(raceId, request));
    }

    @Operation(summary = "Get race result draft", description = "Referee xem draft ket qua cua race minh duoc phan cong.")
    @GetMapping("/get")
    public ResponseEntity<RaceResultDraftResponse> getDraft(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceResultService.getDraft(raceId));
    }
}

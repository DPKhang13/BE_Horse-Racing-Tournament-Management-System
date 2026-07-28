package com.group5.htms.controller;

import com.group5.htms.dto.refereereport.request.RefereeReportCreateRequest;
import com.group5.htms.dto.refereereport.response.RefereeAssignedRaceResponse;
import com.group5.htms.dto.refereereport.response.RefereeReportResponse;
import com.group5.htms.service.RefereeReportService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/referee/races")
@PreAuthorize("hasRole('RACE_REFEREE')")
public class RefereeReportController {
    private final RefereeReportService refereeReportService;

    @Operation(summary = "Get my assigned races", description = "Referee xem danh sach race minh duoc phan cong.")
    @GetMapping("/get-my-assigned")
    public ResponseEntity<List<RefereeAssignedRaceResponse>> getMyAssignedRaces() {
        return ResponseEntity.ok(refereeReportService.getMyAssignedRaces());
    }

    @Operation(summary = "Submit referee report", description = "Referee submit report cho race dang in_progress.")
    @PostMapping("/{raceId}/reports/create")
    public ResponseEntity<RefereeReportResponse> submitReport(
            @PathVariable Integer raceId,
            @Valid @RequestBody RefereeReportCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refereeReportService.submitReport(raceId, request));
    }

    @Operation(summary = "Get race reports", description = "Referee xem reports cua race minh duoc phan cong.")
    @GetMapping("/{raceId}/reports/get")
    public ResponseEntity<List<RefereeReportResponse>> getRaceReports(@PathVariable Integer raceId) {
        return ResponseEntity.ok(refereeReportService.getRaceReports(raceId));
    }
}

package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.response.TournamentResultHistoryResponse;
import com.group5.htms.service.RaceResultService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/tournaments/results/history")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTournamentResultHistoryController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Admin get tournament result history", description = "Admin xem danh sach tournament da co result de vao man hinh history.")
    @GetMapping
    public ResponseEntity<List<TournamentResultHistoryResponse>> getAdminTournamentResultHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String resultStatus
    ) {
        return ResponseEntity.ok(raceResultService.getAdminTournamentResultHistory(status, resultStatus));
    }
}

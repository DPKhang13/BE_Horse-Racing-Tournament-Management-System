package com.group5.htms.controller;

import com.group5.htms.dto.race.response.RaceListResponse;
import com.group5.htms.dto.race.response.ScheduledRaceCountResponse;
import com.group5.htms.service.RaceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RaceController {
    private final RaceService raceService;

    @Operation(
            summary = "Get scheduled race count",
            description = "Lấy tổng số races."
    )
    @GetMapping("/races/get-scheduled-race-count")
    public ResponseEntity<ScheduledRaceCountResponse> getScheduledRaceCount() {
        return ResponseEntity.ok(raceService.getScheduledRaceCount());
    }

    @Operation(
            summary = "Get races by tournament",
            description = "Lấy danh sách race thuộc một tournament để chủ ngựa chọn khi đăng ký giải đấu."
    )
    @GetMapping("/tournaments/{tournamentId}/get-race-list")
    @PreAuthorize("hasAnyRole('HORSE_OWNER', 'ADMIN')")
    public ResponseEntity<List<RaceListResponse>> getRacesByTournament(
            @PathVariable Integer tournamentId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(raceService.getRacesByTournament(tournamentId, status));
    }
}

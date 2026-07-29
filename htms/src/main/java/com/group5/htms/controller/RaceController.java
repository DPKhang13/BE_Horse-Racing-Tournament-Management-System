package com.group5.htms.controller;

import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentListResponse;
import com.group5.htms.dto.race.response.RaceGateAvailabilityResponse;
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
    @PreAuthorize("hasAnyRole('HORSE_OWNER', 'ADMIN', 'SPECTATOR','JOCKEY', 'RACE_REFEREE')")
    public ResponseEntity<List<RaceListResponse>> getRacesByTournament(
            @PathVariable Integer tournamentId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(raceService.getRacesByTournament(tournamentId, status));
    }

    @Operation(
            summary = "Get approved race participants",
            description = "Lấy danh sách ngựa tham gia race cùng jockey thi đấu; chỉ lấy registration đã admin approve và assignment đã confirmed."
    )
    @GetMapping("/races/{raceId}/participants")
    @PreAuthorize("hasAnyRole('SPECTATOR', 'ADMIN', 'HORSE_OWNER')")
    public ResponseEntity<List<JockeyAssignmentListResponse>> getApprovedParticipantsByRace(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceService.getApprovedParticipantsByRace(raceId));
    }

    @Operation(
            summary = "Get available race gates",
            description = "Lấy danh sách số cổng còn trống của race. Gate count = max(8, race.maxHorses); pending/approved/confirmed registrations giữ cổng, rejected/cancelled sẽ trả cổng."
    )
    @GetMapping("/races/{raceId}/available-gates")
    @PreAuthorize("hasAnyRole('HORSE_OWNER', 'ADMIN', 'SPECTATOR')")
    public ResponseEntity<RaceGateAvailabilityResponse> getAvailableGates(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceService.getAvailableGates(raceId));
    }
}

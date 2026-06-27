package com.group5.htms.controller;

import com.group5.htms.dto.race.request.RaceCreateRequest;
import com.group5.htms.dto.race.request.RaceStartRequest;
import com.group5.htms.dto.race.request.RaceUpdateRequest;
import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.dto.race.response.RaceStartResponse;
import com.group5.htms.dto.schedule.request.TournamentScheduleCreateRequest;
import com.group5.htms.dto.schedule.request.TournamentScheduleUpdateRequest;
import com.group5.htms.dto.schedule.response.TournamentScheduleResponse;
import com.group5.htms.service.RaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class RaceSchedulesController {

    private final RaceService raceService;

    @Operation(summary = "Create tournament schedule", description = "Admin tao mot ngay lich thi dau cho tournament.")
    @PostMapping("/tournaments/{tournamentId}/create-schedule")
    public ResponseEntity<TournamentScheduleResponse> createSchedule(
            @PathVariable Integer tournamentId,
            @Valid @RequestBody TournamentScheduleCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceService.createSchedule(tournamentId, request));
    }

    @GetMapping("/tournaments/{tournamentId}/get-schedule-list")
    public ResponseEntity<List<TournamentScheduleResponse>> getSchedulesByTournament(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(raceService.getSchedulesByTournament(tournamentId));
    }

    @GetMapping("/schedules/get-schedule/{scheduleId}")
    public ResponseEntity<TournamentScheduleResponse> getScheduleById(@PathVariable Integer scheduleId) {
        return ResponseEntity.ok(raceService.getScheduleById(scheduleId));
    }

    @PutMapping("/schedules/update-schedule/{scheduleId}")
    public ResponseEntity<TournamentScheduleResponse> updateSchedule(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody TournamentScheduleUpdateRequest request
    ) {
        return ResponseEntity.ok(raceService.updateSchedule(scheduleId, request));
    }

    @Operation(summary = "Create race", description = "Admin tao race trong mot schedule da thuoc tournament.")
    @PostMapping("/schedules/{scheduleId}/create-race")
    public ResponseEntity<RaceResponse> createRace(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody RaceCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceService.createRace(scheduleId, request));
    }

    @GetMapping("/races/get-race/{raceId}")
    public ResponseEntity<RaceResponse> getRaceById(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceService.getRaceById(raceId));
    }

    @PutMapping("/races/update-race/{raceId}")
    public ResponseEntity<RaceResponse> updateRace(
            @PathVariable Integer raceId,
            @Valid @RequestBody RaceUpdateRequest request
    ) {
        return ResponseEntity.ok(raceService.updateRace(raceId, request));
    }

    @Operation(summary = "Start race", description = "Admin chuyển race sang trạng thái in_progress và đóng betting nếu cần.")
    @PatchMapping("/races/{raceId}/start")
    public ResponseEntity<RaceStartResponse> startRace(
            @PathVariable Integer raceId,
            @Valid @RequestBody(required = false) RaceStartRequest request
    ) {
        return ResponseEntity.ok(raceService.startRace(raceId, request));
    }

    @Operation(summary = "Complete race", description = "Admin chuyển race sang trạng thái completed sau khi đã publish kết quả.")
    @PatchMapping("/races/{raceId}/complete")
    public ResponseEntity<RaceResponse> completeRace(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceService.completeRace(raceId));
    }

    @PatchMapping("/races/cancel-race/{raceId}")
    public ResponseEntity<Map<String, String>> cancelRace(@PathVariable Integer raceId) {
        raceService.cancelRace(raceId);
        return ResponseEntity.ok(Map.of("message", "Race cancelled successfully"));
    }
}

package com.group5.htms.controller;

import com.group5.htms.dto.raceround.request.RaceRoundCreateRequest;
import com.group5.htms.dto.raceround.request.RaceRoundUpdateRequest;
import com.group5.htms.dto.raceround.response.RaceRoundResponse;
import com.group5.htms.service.RaceRoundService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/race-rounds")
@RequiredArgsConstructor
public class RaceRoundController {
    private final RaceRoundService raceRoundService;

    @Operation(summary = "Get race rounds", description = "Lấy danh sách kết quả từng vòng. Có thể lọc theo raceId hoặc assignmentId.")
    @GetMapping("/get-all")
    public ResponseEntity<List<RaceRoundResponse>> getAllRounds(
            @RequestParam(required = false) Integer raceId,
            @RequestParam(required = false) Integer assignmentId
    ) {
        return ResponseEntity.ok(raceRoundService.getAllRounds(raceId, assignmentId));
    }

    @Operation(summary = "Get race round by id", description = "Lấy thông tin kết quả từng vòng theo round id.")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<RaceRoundResponse> getRoundById(@PathVariable Integer id) {
        return ResponseEntity.ok(raceRoundService.getRoundById(id));
    }

    @Operation(summary = "Create race round", description = "Tạo kết quả từng vòng cho một jockey assignment. Race và horse được backend lấy từ assignment.")
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<RaceRoundResponse> createRound(@Valid @RequestBody RaceRoundCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceRoundService.createRound(request));
    }

    @Operation(summary = "Update race round", description = "Cập nhật kết quả từng vòng. Field nào không gửi sẽ giữ nguyên.")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<RaceRoundResponse> updateRound(
            @PathVariable Integer id,
            @Valid @RequestBody RaceRoundUpdateRequest request
    ) {
        return ResponseEntity.ok(raceRoundService.updateRound(id, request));
    }
}

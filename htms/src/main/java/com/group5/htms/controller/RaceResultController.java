package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.service.RaceResultService;
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
@RequestMapping("/api/race-results")
@RequiredArgsConstructor
public class RaceResultController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Get all race results", description = "Lấy danh sách tất cả kết quả race.")
    @GetMapping("/get-all")
    public ResponseEntity<List<RaceResultResponse>> getAllResults() {
        return ResponseEntity.ok(raceResultService.getAllResults());
    }

    @Operation(summary = "Get race result by id", description = "Lấy thông tin kết quả race theo result id.")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<RaceResultResponse> getResultById(@PathVariable Integer id) {
        return ResponseEntity.ok(raceResultService.getResultById(id));
    }

    @Operation(summary = "Create race result", description = "Tạo mới kết quả race cho một jockey assignment.")
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<RaceResultResponse> createResult(@Valid @RequestBody RaceResultCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceResultService.createResult(request));
    }

    @Operation(summary = "Update race result", description = "Cập nhật kết quả race. Field nào không gửi lên sẽ giữ nguyên.")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<RaceResultResponse> updateResult(
            @PathVariable Integer id,
            @Valid @RequestBody RaceResultUpdateRequest request
    ) {
        return ResponseEntity.ok(raceResultService.updateResult(id, request));
    }

    @Operation(summary = "Publish race result", description = "Publish kết quả race bằng cách cập nhật status và published at.")
    @PutMapping("/publish/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<RaceResultResponse> publishResult(
            @PathVariable Integer id,
            @Valid @RequestBody RaceResultPublishRequest request
    ) {
        return ResponseEntity.ok(raceResultService.publishResult(id, request));
    }

    @Operation(summary = "Delete race result", description = "Xóa kết quả race theo result id.")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<Void> deleteResult(@PathVariable Integer id) {
        raceResultService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }
}

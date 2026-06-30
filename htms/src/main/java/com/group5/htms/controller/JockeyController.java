package com.group5.htms.controller;

import com.group5.htms.dto.jockey.response.JockeyListResponse;
import com.group5.htms.dto.jockey.response.JockeyRankingResponse;
import com.group5.htms.service.JockeyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jockeys")
@RequiredArgsConstructor
public class JockeyController {
    private final JockeyService jockeyService;

    @Operation(
            summary = "Get all jockeys",
            description = "Lấy danh sách jockey để chủ ngựa chọn khi tạo lời mời đăng ký race."
    )
    @GetMapping("/get-all")
    public ResponseEntity<List<JockeyListResponse>> getAllJockeys(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(jockeyService.getAllJockeys(status));
    }

    @Operation(
            summary = "Get jockey ranking",
            description = "Lấy bảng xếp hạng jockey theo ranking points, total wins và experience years."
    )
    @GetMapping("/ranking")
    public ResponseEntity<List<JockeyRankingResponse>> getJockeyRanking() {
        return ResponseEntity.ok(jockeyService.getJockeyRanking());
    }
}

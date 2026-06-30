package com.group5.htms.controller;

import com.group5.htms.dto.bet.request.BetCheckRequest;
import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetListResponse;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.dashboard.response.PredictionRaceResponse;
import com.group5.htms.dto.dashboard.response.SpectatorDashboardResponse;
import com.group5.htms.service.BetService;
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

import java.util.List;

@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
public class BetController {
    private final BetService betService;

    @Operation(summary = "Get spectator dashboard", description = "Dashboard tổng hợp theo JWT spectator: wallet, summary count, upcoming races, active bets, latest results, notifications và race đang mở dự đoán.")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SPECTATOR', 'ADMIN')")
    public ResponseEntity<SpectatorDashboardResponse> getSpectatorDashboard() {
        return ResponseEntity.ok(betService.getSpectatorDashboard());
    }

    @Operation(summary = "Get open prediction races", description = "Lấy race đang mở dự đoán kèm bet optionId để FE đặt cược.")
    @GetMapping("/open-predictions")
    @PreAuthorize("hasAnyRole('SPECTATOR', 'ADMIN')")
    public ResponseEntity<List<PredictionRaceResponse>> getOpenPredictionRaces() {
        return ResponseEntity.ok(betService.getOpenPredictionRaces());
    }

    @Operation(summary = "Get all bets", description = "Lấy danh sách tất cả bet.")
    @GetMapping("/get-all")
    public ResponseEntity<List<BetListResponse>> getAllBets() {
        return ResponseEntity.ok(betService.getAllBets());
    }

    @Operation(summary = "Get bet by id", description = "Lấy bet theo bet id.")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<BetResponse> getBetById(@PathVariable Integer id) {
        return ResponseEntity.ok(betService.getBetById(id));
    }

    @Operation(summary = "Create bet", description = "Tạo bet cho một assignment. Spectator role được lấy từ JWT của user đang đăng nhập.")
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('SPECTATOR', 'ADMIN')")
    public ResponseEntity<BetResponse> createBet(@Valid @RequestBody BetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(betService.createBet(request));
    }

    @Operation(summary = "Update bet", description = "Cập nhật bet. Field nào không gửi lên sẽ giữ nguyên.")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('SPECTATOR', 'ADMIN')")
    public ResponseEntity<BetResponse> updateBet(
            @PathVariable Integer id,
            @Valid @RequestBody BetUpdateRequest request
    ) {
        return ResponseEntity.ok(betService.updateBet(id, request));
    }

    @Operation(summary = "Check bet result", description = "Cập nhật kết quả bet sau race. Người settle được lấy từ JWT.")
    @PutMapping("/check/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<BetResponse> checkBet(
            @PathVariable Integer id,
            @Valid @RequestBody BetCheckRequest request
    ) {
        return ResponseEntity.ok(betService.checkBet(id, request));
    }
}

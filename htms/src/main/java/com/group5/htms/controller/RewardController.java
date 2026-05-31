package com.group5.htms.controller;

import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.reward.request.RewardCalculateRequest;
import com.group5.htms.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;

    @Operation(summary = "Calculate bet reward", description = "Tính/cập nhật reward payout cho bet theo bet id. Người settle được lấy từ JWT.")
    @PutMapping("/calculate/{betId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_REFEREE')")
    public ResponseEntity<BetResponse> calculateReward(
            @PathVariable Integer betId,
            @Valid @RequestBody RewardCalculateRequest request
    ) {
        return ResponseEntity.ok(rewardService.calculateReward(betId, request));
    }
}

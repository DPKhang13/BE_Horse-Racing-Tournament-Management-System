package com.group5.htms.controller;

import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.service.BetOptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bet-options")
@RequiredArgsConstructor
public class BetOptionController {
    private final BetOptionService betOptionService;

    @Operation(summary = "Get bet options", description = "Lấy danh sách option để spectator xem trước khi đặt bet.")
    @GetMapping("/get-all")
    public ResponseEntity<List<BetOptionResponse>> getAllBetOptions(
            @RequestParam(required = false) Integer raceId
    ) {
        if (raceId != null) {
            return ResponseEntity.ok(betOptionService.getBetOptionsByRace(raceId));
        }

        return ResponseEntity.ok(betOptionService.getAllBetOptions());
    }

    @Operation(summary = "Get bet options by race", description = "Lấy danh sách bet option theo id.")
    @GetMapping("/get-by-race/{raceId}")
    public ResponseEntity<List<BetOptionResponse>> getBetOptionsByRace(
            @PathVariable Integer raceId
    ) {
        return ResponseEntity.ok(betOptionService.getBetOptionsByRace(raceId));
    }

    @Operation(summary = "Get bet option by id", description = "Lấy chi tiết bet option theo option id.")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<BetOptionResponse> getBetOptionById(@PathVariable Integer id) {
        return ResponseEntity.ok(betOptionService.getBetOptionById(id));
    }
}

package com.group5.htms.controller;

import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.service.RaceResultService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/races/{raceId}/results/public")
public class PublicRaceResultController {
    private final RaceResultService raceResultService;

    @Operation(summary = "Get public race results", description = "Public xem kết quả race đã published.")
    @GetMapping("/get")
    public ResponseEntity<List<RaceResultResponse>> getPublicResults(@PathVariable Integer raceId) {
        return ResponseEntity.ok(raceResultService.getPublicResults(raceId));
    }
}

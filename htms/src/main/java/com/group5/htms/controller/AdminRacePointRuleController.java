package com.group5.htms.controller;

import com.group5.htms.dto.racepointrule.request.RacePointRuleItemRequest;
import com.group5.htms.dto.racepointrule.response.RacePointRuleResponse;
import com.group5.htms.service.RacePointRuleService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/races/{raceId}/point-rules")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRacePointRuleController {
    private final RacePointRuleService racePointRuleService;

    @Operation(summary = "Get race point rules", description = "Admin xem cấu hình điểm theo thứ hạng trong race.")
    @GetMapping("/get")
    public ResponseEntity<List<RacePointRuleResponse>> getPointRules(@PathVariable Integer raceId) {
        return ResponseEntity.ok(racePointRuleService.getPointRules(raceId));
    }

    @Operation(summary = "Create race point rules", description = "Admin tạo điểm theo thứ hạng trong race.")
    @PostMapping("/create")
    public ResponseEntity<List<RacePointRuleResponse>> createPointRules(
            @PathVariable Integer raceId,
            @Valid @RequestBody List<RacePointRuleItemRequest> request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(racePointRuleService.createPointRules(raceId, request));
    }

    @Operation(summary = "Replace race point rules", description = "Admin thay đổi điểm thứ hạng trong race.")
    @PutMapping("/update")
    public ResponseEntity<List<RacePointRuleResponse>> replacePointRules(
            @PathVariable Integer raceId,
            @Valid @RequestBody List<RacePointRuleItemRequest> request
    ) {
        return ResponseEntity.ok(racePointRuleService.replacePointRules(raceId, request));
    }

    @Operation(summary = "Delete race point rule", description = "Admin xóa điểm của race.")
    @DeleteMapping("/delete/{ruleId}")
    public ResponseEntity<Void> deletePointRule(
            @PathVariable Integer raceId,
            @PathVariable Integer ruleId
    ) {
        racePointRuleService.deletePointRule(raceId, ruleId);
        return ResponseEntity.noContent().build();
    }
}

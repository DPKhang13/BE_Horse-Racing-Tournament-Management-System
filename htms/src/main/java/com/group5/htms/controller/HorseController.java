package com.group5.htms.controller;

import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseListResponse;
import com.group5.htms.dto.horse.response.HorseRankingResponse;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.service.HorseService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/horses")
@RequiredArgsConstructor
public class HorseController {
    private final HorseService horseService;

    @Operation(
            summary = "Get all horses",
            description = "Lấy danh sách tất cả ngựa trong hệ thống."
    )
    @GetMapping("/get-all")
    public ResponseEntity<List<HorseListResponse>> getAllHorses() {
        return ResponseEntity.ok(horseService.getAllHorses());
    }

    @Operation(
            summary = "Get horse ranking",
            description = "Lấy bảng xếp hạng ngựa theo ranking points, total wins và tên ngựa."
    )
    @GetMapping("/ranking")
    public ResponseEntity<List<HorseRankingResponse>> getHorseRanking() {
        return ResponseEntity.ok(horseService.getHorseRanking());
    }

    @Operation(
            summary = "Get horse by id",
            description = "Lấy thông tin chi tiết của một ngựa theo horse id."
    )
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<HorseResponse> getHorseById(@PathVariable Integer id) {
        return ResponseEntity.ok(horseService.getHorseById(id));
    }

    @Operation(
            summary = "Create horse",
            description = "Tạo mới một ngựa. Owner role được lấy từ JWT của user đang đăng nhập."
    )
    @PostMapping("/create")
    @PreAuthorize("hasRole('HORSE_OWNER')")
    public ResponseEntity<HorseResponse> createHorse(@Valid @RequestBody HorseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horseService.createHorse(request));
    }

    @Operation(
            summary = "Update horse",
            description = "Cập nhật thông tin ngựa theo horse id. Field nào không gửi lên sẽ được giữ nguyên."
    )
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('HORSE_OWNER')")
    public ResponseEntity<HorseResponse> updateHorse(
            @PathVariable Integer id,
            @Valid @RequestBody HorseUpdateRequest request
    ) {
        return ResponseEntity.ok(horseService.updateHorse(id, request));
    }

    @Operation(
            summary = "Delete horse",
            description = "Xóa một ngựa theo horse id."
    )
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('HORSE_OWNER')")
    public ResponseEntity<Void> deleteHorse(@PathVariable Integer id) {
        horseService.deleteHorse(id);
        return ResponseEntity.noContent().build();
    }
}

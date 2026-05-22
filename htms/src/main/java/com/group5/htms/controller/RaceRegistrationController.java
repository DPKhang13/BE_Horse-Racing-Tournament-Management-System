package com.group5.htms.controller;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.service.RaceRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/race-registrations")
@RequiredArgsConstructor
public class RaceRegistrationController {
    private final RaceRegistrationService raceRegistrationService;

    @Operation(summary = "Get all race registrations", description = "Lấy danh sách tất cả đăng ký ngựa vào race.")
    @GetMapping("/get-all")
    public ResponseEntity<List<RaceRegistrationResponse>> getAllRegistrations() {
        return ResponseEntity.ok(raceRegistrationService.getAllRegistrations());
    }

    @Operation(summary = "Get race registration by id", description = "Lấy thông tin đăng ký race theo registration id.")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<RaceRegistrationResponse> getRegistrationById(@PathVariable Integer id) {
        return ResponseEntity.ok(raceRegistrationService.getRegistrationById(id));
    }

    @Operation(summary = "Create race registration", description = "Đăng ký một ngựa vào race với tournament, race, horse và owner role.")
    @PostMapping("/create")
    public ResponseEntity<RaceRegistrationResponse> createRegistration(
            @Valid @RequestBody RaceRegistrationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceRegistrationService.createRegistration(request));
    }

    @Operation(summary = "Update race registration", description = "Cập nhật thông tin đăng ký race. Field nào không gửi lên sẽ giữ nguyên.")
    @PutMapping("/update/{id}")
    public ResponseEntity<RaceRegistrationResponse> updateRegistration(
            @PathVariable Integer id,
            @Valid @RequestBody RaceRegistrationUpdateRequest request
    ) {
        return ResponseEntity.ok(raceRegistrationService.updateRegistration(id, request));
    }

    @Operation(summary = "Approve race registration", description = "Cập nhật trạng thái duyệt đăng ký race và người duyệt nếu có.")
    @PutMapping("/approve/{id}")
    public ResponseEntity<RaceRegistrationResponse> approveRegistration(
            @PathVariable Integer id,
            @Valid @RequestBody RaceRegistrationApprovalRequest request
    ) {
        return ResponseEntity.ok(raceRegistrationService.approveRegistration(id, request));
    }

    @Operation(summary = "Delete race registration", description = "Xóa đăng ký race theo registration id.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Integer id) {
        raceRegistrationService.deleteRegistration(id);
        return ResponseEntity.noContent().build();
    }
}

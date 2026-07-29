package com.group5.htms.controller;

import com.group5.htms.dto.admin.request.AdminUserCreateRequest;
import com.group5.htms.dto.admin.request.AdminUserResetPasswordRequest;
import com.group5.htms.dto.admin.request.AdminUserStatusUpdateRequest;
import com.group5.htms.dto.admin.request.AdminUserUpdateRequest;
import com.group5.htms.dto.admin.response.AdminUserDetailResponse;
import com.group5.htms.dto.admin.response.AdminUserListResponse;
import com.group5.htms.dto.admin.response.AdminUserResetPasswordResponse;
import com.group5.htms.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserListResponse>> getUsers(
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminUserService.getUsers(roleType, status, keyword, pageable));
    }


    @GetMapping("/horse-owners")
    public ResponseEntity<List<AdminUserListResponse>> getHorseOwners() {
        return ResponseEntity.ok(adminUserService.getHorseOwners());
    }

    @GetMapping("/jockeys")
    public ResponseEntity<List<AdminUserListResponse>> getJockeys() {
        return ResponseEntity.ok(adminUserService.getJockeys());
    }

    @GetMapping("/referees")
    public ResponseEntity<List<AdminUserListResponse>> getReferees() {
        return ResponseEntity.ok(adminUserService.getReferees());
    }

    @PutMapping("/horse-owners/{userId}/profile")
    public ResponseEntity<AdminUserDetailResponse> updateHorseOwnerProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateHorseOwnerProfile(userId, request));
    }

    @PutMapping("/jockeys/{userId}/profile")
    public ResponseEntity<AdminUserDetailResponse> updateJockeyProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateJockeyProfile(userId, request));
    }

    @PutMapping("/referees/{userId}/profile")
    public ResponseEntity<AdminUserDetailResponse> updateRefereeProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateRefereeProfile(userId, request));
    }
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailResponse> getUserById(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<AdminUserDetailResponse> createUser(
            @Valid @RequestBody AdminUserCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminUserService.createUser(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AdminUserDetailResponse> updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserDetailResponse> updateStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateStatus(userId, request));
    }


    @PatchMapping("/{userId}/ban")
    public ResponseEntity<AdminUserDetailResponse> banUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminUserService.banUser(userId));
    }
    @PatchMapping("/{userId}/reset-password")
    public ResponseEntity<AdminUserResetPasswordResponse> resetPassword(
            @PathVariable Integer userId,
            @Valid @RequestBody AdminUserResetPasswordRequest request
    ) {
        return ResponseEntity.ok(adminUserService.resetPassword(userId, request));
    }
}

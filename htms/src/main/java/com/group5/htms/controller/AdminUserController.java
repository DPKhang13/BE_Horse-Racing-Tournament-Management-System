package com.group5.htms.controller;

import com.group5.htms.dto.admin.request.AdminUserCreateRequest;
import com.group5.htms.dto.auth.response.UserMeResponse;
import com.group5.htms.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @PostMapping("/create")
    public ResponseEntity<UserMeResponse> createUser(
            @Valid @RequestBody AdminUserCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminUserService.createUser(request));
    }
}

package com.group5.htms.controller;

import com.group5.htms.dto.auth.AuthResponse;
import com.group5.htms.dto.auth.LoginRequest;
import com.group5.htms.dto.auth.RegisterRequest;
import com.group5.htms.dto.auth.UserMeResponse;
import com.group5.htms.dto.otpverify.request.ResendOtpRequest;
import com.group5.htms.dto.otpverify.request.VerifyOtpRequest;
import com.group5.htms.dto.otpverify.response.OtpVerifyResponse;
import com.group5.htms.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<OtpVerifyResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<OtpVerifyResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);

        return ResponseEntity.ok(
                Map.of("message", "Logout successfully")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}

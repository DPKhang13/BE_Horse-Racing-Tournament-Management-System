package com.group5.htms.service;

import com.group5.htms.dto.auth.AuthResponse;
import com.group5.htms.dto.auth.LoginRequest;
import com.group5.htms.dto.auth.RegisterRequest;
import com.group5.htms.dto.auth.UserMeResponse;
import com.group5.htms.dto.otpverify.request.ResendOtpRequest;
import com.group5.htms.dto.otpverify.request.VerifyOtpRequest;
import com.group5.htms.dto.otpverify.response.OtpVerifyResponse;
import com.group5.htms.entity.Roles;
import com.group5.htms.entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    OtpVerifyResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    UserMeResponse me();

    Users getCurrentUser();

    Roles getCurrentUserRole(String roleType);

    Integer getCurrentUserId();

    Integer getCurrentUserRoleId(String roleType);

    OtpVerifyResponse verifyOtp(VerifyOtpRequest request);

    OtpVerifyResponse resendOtp(ResendOtpRequest request);
}

package com.group5.htms.service;

import com.group5.htms.dto.auth.AuthResponse;
import com.group5.htms.dto.auth.LoginRequest;
import com.group5.htms.dto.auth.RegisterRequest;
import com.group5.htms.dto.auth.UserMeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request, HttpServletResponse response);

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    UserMeResponse me();
}
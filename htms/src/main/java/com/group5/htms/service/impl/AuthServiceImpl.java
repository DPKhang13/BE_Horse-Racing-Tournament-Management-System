package com.group5.htms.service.impl;

import com.group5.htms.config.CustomUserDetailsConfig;
import com.group5.htms.dto.auth.response.AuthResponse;
import com.group5.htms.dto.auth.request.LoginRequest;
import com.group5.htms.dto.auth.request.RegisterRequest;
import com.group5.htms.dto.auth.response.UserMeResponse;
import com.group5.htms.dto.otpverify.request.ResendOtpRequest;
import com.group5.htms.dto.otpverify.request.VerifyOtpRequest;
import com.group5.htms.dto.otpverify.response.OtpVerifyResponse;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.OtpValidationStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.UnauthorizedException;
import com.group5.htms.mapper.AuthMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.OtpMailService;
import com.group5.htms.util.CookieUtil;
import com.group5.htms.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ACCESS_TOKEN_COOKIE = "AccessToken";
    private static final String REFRESH_TOKEN_COOKIE = "RefreshToken";

    private static final String STATUS_ACTIVE = "active";

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsConfig userDetailsService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final AuthMapper authMapper;
    private final OtpMailService otpMailService;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final RefereeProfilesRepository refereeProfilesRepository;
    /*
     * POST /api/auth/register
     1. Check username/email trùng.
     2. Chuẩn hóa role đăng ký.
     3. Tạo Users với role_type.
     4. Save user.
     5. Tạo access token + refresh token.
     6. Set token vào HttpOnly Cookie.
     */
    @Override
    @Transactional
    public OtpVerifyResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String username = request.getUsername().trim();

        usersRepository.findByEmail(email).ifPresent(existingUser -> {
            if (UserStatus.ACTIVE.getValue().equalsIgnoreCase(existingUser.getStatus())) {
                throw new BadRequestException("Email already exists");
            }

            if (UserStatus.INACTIVE.getValue().equalsIgnoreCase(existingUser.getStatus())) {
                otpMailService.resendOtp(email);
                throw new BadRequestException("Email already registered but not verified. OTP has been resent.");
            }

            throw new BadRequestException("Email cannot be used");
        });

        if (usersRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }

        String initialRole = normalizeRegisterRole(request.getRoleType());

        Users user = Users.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(clean(request.getPhone()))
                .roleType(initialRole)
                .status(UserStatus.INACTIVE.getValue())
                .build();

        usersRepository.save(user);

        otpMailService.generateAndSendOtp(email);

        return OtpVerifyResponse.builder()
                .message("Register successfully. Please verify your email with OTP.")
                .email(email)
                .verified(false)
                .build();
    }

    /*
     POST /api/auth/login
     1. AuthenticationManager check username/email + password.
     2. Load user + roles từ DB.
     3. Tạo access token + refresh token.
     4. Set token vào HttpOnly Cookie.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        Users user = usersRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail()
                )
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        return buildAuthResponse(userDetails, user, response);
    }

    /*
     POST /api/auth/refresh-token
     - Không cần body.
     - Backend đọc RefreshToken từ HttpOnly Cookie.
     - Validate refresh token.
     - Revoke refresh token cũ.
     - Tạo access token + refresh token mới.
     - Set lại cookie mới.
     */
    @Override
    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE)
                .orElseThrow(() -> new UnauthorizedException("Refresh token cookie not found"));

        String username;

        try {
            username = jwtUtil.extractUsername(refreshToken, true);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (!jwtUtil.isTokenValid(refreshToken, username, true)) {
            throw new UnauthorizedException("Refresh token is expired or invalid");
        }

        String oldJti = jwtUtil.extractJti(refreshToken, true);

        if (!refreshTokenService.isValid(oldJti, refreshToken, username)) {
            throw new UnauthorizedException("Refresh token was revoked or rotated");
        }

        refreshTokenService.revoke(oldJti);

        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedException("User account is not active");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return buildAuthResponse(userDetails, user, response);
    }

    /*
     * POST /api/auth/logout
     - Nếu có RefreshToken cookie thì revoke token đó.
     - Nếu request đã authenticated thì revoke toàn bộ refresh token của user.
     - Clear AccessToken + RefreshToken cookie.
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.getCookieValue(request, REFRESH_TOKEN_COOKIE)
                .ifPresent(refreshToken -> {
                    try {
                        String jti = jwtUtil.extractJti(refreshToken, true);
                        refreshTokenService.revoke(jti);
                    } catch (Exception ignored) {
                        // Logout phải idempotent: token sai/hết hạn vẫn cho logout thành công.
                    }
                });

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            if (username != null && !"anonymousUser".equals(username)) {
                refreshTokenService.revokeAllByUsername(username);
            }
        }

        CookieUtil.clearCookie(response, ACCESS_TOKEN_COOKIE);
        CookieUtil.clearCookie(response, REFRESH_TOKEN_COOKIE);

        SecurityContextHolder.clearContext();
    }

    /*
     GET /api/auth/me
     JwtAuthenticationFilter đọc AccessToken cookie hoặc Bearer token,
     sau đó set Authentication vào SecurityContextHolder.
     */
    @Override
    @Transactional(readOnly = true)
    public UserMeResponse me() {
        return toUserMeResponseWithProfile(getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthenticated");
        }

        String username = authentication.getName();

        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /*
     Tạo token mới và set vào cookie.
     AccessToken:
     - Dùng để gọi API.
     - Sống ngắn.
     RefreshToken:
     - Dùng để xin AccessToken mới.
     - Được lưu trong RefreshTokenServiceImpl để hỗ trợ revoke/rotation.
     */
    private AuthResponse buildAuthResponse(
            UserDetails userDetails,
            Users user,
            HttpServletResponse response
    ) {
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        String refreshJti = jwtUtil.extractJti(refreshToken, true);

        refreshTokenService.save(
                refreshJti,
                refreshToken,
                userDetails.getUsername(),
                jwtUtil.getRefreshTokenAge()
        );

        CookieUtil.addHttpOnlyCookie(
                response,
                ACCESS_TOKEN_COOKIE,
                accessToken,
                jwtUtil.getAccessTokenAge()
        );

        CookieUtil.addHttpOnlyCookie(
                response,
                REFRESH_TOKEN_COOKIE,
                refreshToken,
                jwtUtil.getRefreshTokenAge()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)      // giữ lại để test Swagger/Postman
                .refreshToken(refreshToken)    // production có thể bỏ khỏi response
                .expiresIn(jwtUtil.getAccessTokenAge() / 1000)
                .user(toUserMeResponseWithProfile(user))
                .build();
    }

    private UserMeResponse toUserMeResponseWithProfile(Users user) {
        if (user == null || user.getRoleType() == null) {
            return authMapper.toUserMeResponse(user);
        }

        String roleType = user.getRoleType().trim().toLowerCase();

        if (RoleType.HORSE_OWNER.getValue().equals(roleType)) {
            return authMapper.toUserMeResponse(
                    user,
                    horseOwnerProfilesRepository.findById(user.getId()).orElse(null),
                    null,
                    null
            );
        }

        if (RoleType.JOCKEY.getValue().equals(roleType)) {
            return authMapper.toUserMeResponse(
                    user,
                    null,
                    jockeyProfilesRepository.findById(user.getId()).orElse(null),
                    null
            );
        }

        if (RoleType.RACE_REFEREE.getValue().equals(roleType)) {
            return authMapper.toUserMeResponse(
                    user,
                    null,
                    null,
                    refereeProfilesRepository.findById(user.getId()).orElse(null)
            );
        }

        return authMapper.toUserMeResponse(user);
    }

    /*
     Chuẩn hóa role khi register.
     Nếu không gửi roleType thì mặc định là spectator.
     */
    private String normalizeRegisterRole(String roleType) {
        if (roleType == null || roleType.isBlank()) {
            return RoleType.SPECTATOR.getValue();
        }

        String normalizedRole = roleType.trim().toLowerCase();

        if (!RoleType.SPECTATOR.getValue().equals(normalizedRole)) {
            throw new BadRequestException("Only spectator can register publicly");
        }

        return normalizedRole;
    }

    @Override
    @Transactional
    public OtpVerifyResponse verifyOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (UserStatus.ACTIVE.getValue().equalsIgnoreCase(user.getStatus())) {
            return OtpVerifyResponse.builder()
                    .message("Email is already verified")
                    .email(email)
                    .verified(true)
                    .build();
        }

        if (!UserStatus.INACTIVE.getValue().equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("User account cannot be verified");
        }

        OtpValidationStatus otpStatus = otpMailService.validateOtp(email, request.getOtp());

        switch (otpStatus) {
            case VALID -> {
                // continue
            }
            case NOT_FOUND -> throw new BadRequestException("OTP not found. Please request a new OTP.");
            case EXPIRED -> throw new BadRequestException("OTP has expired. Please request a new OTP.");
            case MAX_ATTEMPTS_EXCEEDED -> throw new BadRequestException("Too many failed attempts. Please request a new OTP.");
            case INVALID -> throw new BadRequestException("Invalid OTP.");
        }

        user.setStatus(UserStatus.ACTIVE.getValue());

        usersRepository.save(user);

        otpMailService.clearOtp(email);

        return OtpVerifyResponse.builder()
                .message("Email verified successfully. Please login.")
                .email(email)
                .verified(true)
                .build();
    }

    @Override
    public OtpVerifyResponse resendOtp(ResendOtpRequest request) {
        String email = normalizeEmail(request.getEmail());

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (UserStatus.ACTIVE.getValue().equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("Email is already verified");
        }

        if (!UserStatus.INACTIVE.getValue().equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("User account cannot request OTP");
        }

        otpMailService.resendOtp(email);

        return OtpVerifyResponse.builder()
                .message("OTP has been resent to your email")
                .email(email)
                .verified(false)
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        return email.trim().toLowerCase();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank() ? null : cleaned;
    }
}

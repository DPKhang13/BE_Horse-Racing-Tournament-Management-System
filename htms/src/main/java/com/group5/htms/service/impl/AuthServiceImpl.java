package com.group5.htms.service.impl;

import com.group5.htms.config.CustomUserDetailsConfig;
import com.group5.htms.dto.auth.AuthResponse;
import com.group5.htms.dto.auth.LoginRequest;
import com.group5.htms.dto.auth.RegisterRequest;
import com.group5.htms.dto.auth.UserMeResponse;
import com.group5.htms.entity.Roles;
import com.group5.htms.entity.Users;
import com.group5.htms.exceptions.BadRequestException;
import com.group5.htms.exceptions.ResourceNotFoundException;
import com.group5.htms.exceptions.UnauthorizedException;
import com.group5.htms.mapper.AuthMapper;
import com.group5.htms.repository.RolesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.AuthService;
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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ACCESS_TOKEN_COOKIE = "AccessToken";
    private static final String REFRESH_TOKEN_COOKIE = "RefreshToken";

    private static final String STATUS_ACTIVE = "active";

    private static final String ROLE_SPECTATOR = "spectator";
    private static final String ROLE_HORSE_OWNER = "horse_owner";
    private static final String ROLE_JOCKEY = "jockey";

    /*
     Role được phép tự đăng ký.
     Admin và Race Referee phải được tạo/phân quyền bởi admin, không cho public register.
     */
    private static final Set<String> PUBLIC_REGISTER_ROLES = Set.of(
            ROLE_SPECTATOR,
            ROLE_HORSE_OWNER,
            ROLE_JOCKEY
    );

    private static final Set<String> BLOCKED_REGISTER_ROLES = Set.of(
            "admin",
            "race_referee"
    );

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsConfig userDetailsService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final AuthMapper authMapper;

    /*
     * POST /api/auth/register
     1. Check username/email trùng.
     2. Chuẩn hóa role đăng ký.
     3. Tạo Users + Roles.
     4. Save user, cascade sẽ save role.
     5. Tạo access token + refresh token.
     6. Set token vào HttpOnly Cookie.
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (usersRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        String initialRole = normalizeRegisterRole(request.getRoleType());

        Users user = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(STATUS_ACTIVE)
                .build();

        Roles role = Roles.builder()
                .roleType(initialRole)
                .status(STATUS_ACTIVE)
                .build();

        user.addRole(role);

        Users savedUser = usersRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());

        return buildAuthResponse(userDetails, savedUser, response);
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

        Users user = usersRepository.findByUsernameOrEmailWithRoles(request.getUsernameOrEmail())
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

        Users user = usersRepository.findByUsernameWithRoles(username)
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
     * GET /api/auth/me
     JwtAuthenticationFilter đọc AccessToken cookie hoặc Bearer token,
     sau đó set Authentication vào SecurityContextHolder.
     */
    @Override
    @Transactional(readOnly = true)
    public UserMeResponse me() {
        return authMapper.toUserMeResponse(getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthenticated");
        }

        String username = authentication.getName();

        Users user = usersRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Roles getCurrentUserRole(String roleType) {
        Users user = getCurrentUser();

        return rolesRepository.findByUsersAndRoleType(user, roleType)
                .orElseThrow(() -> new ResourceNotFoundException(roleType + " role not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentUserRoleId(String roleType) {
        return getCurrentUserRole(roleType).getId();
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
                .user(authMapper.toUserMeResponse(user))
                .build();
    }

    /*
     Chuẩn hóa role khi register.
     Nếu không gửi roleType thì mặc định là spectator.
     */
    private String normalizeRegisterRole(String roleType) {
        if (roleType == null || roleType.isBlank()) {
            return ROLE_SPECTATOR;
        }

        String normalizedRole = roleType.trim().toLowerCase();

        if (BLOCKED_REGISTER_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("This role cannot be created from public registration");
        }

        if (!PUBLIC_REGISTER_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("Invalid register role");
        }

        return normalizedRole;
    }
}

package com.group5.htms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessTokenAge;
    private final long refreshTokenAge;

    public JwtUtil(
            @Value("${JWT_ACCESSKEY}") String accessKey,
            @Value("${JWT_REFRESHKEY}") String refreshKey,
            @Value("${JWT_ACCESSEXPIRATION}") long accessTokenAge,
            @Value("${JWT_REFRESHEXPIRATION}") long refreshTokenAge
    ) {
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenAge = accessTokenAge;
        this.refreshTokenAge = refreshTokenAge;
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "ACCESS");
        claims.put(
                "roles",
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );

        return buildToken(claims, userDetails.getUsername(), accessTokenAge, accessKey);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");

        return buildToken(claims, userDetails.getUsername(), refreshTokenAge, refreshKey);
    }

    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expirationMillis,
            SecretKey key
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token, boolean refreshToken) {
        return extractAllClaims(token, refreshToken).getSubject();
    }

    public String extractJti(String token, boolean refreshToken) {
        return extractAllClaims(token, refreshToken).getId();
    }

    public boolean isTokenValid(String token, String username, boolean refreshToken) {
        try {
            Claims claims = extractAllClaims(token, refreshToken);

            String tokenType = claims.get("tokenType", String.class);
            boolean correctTokenType = refreshToken
                    ? "REFRESH".equals(tokenType)
                    : "ACCESS".equals(tokenType);

            return correctTokenType
                    && username.equals(claims.getSubject())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims extractAllClaims(String token, boolean refreshToken) {
        SecretKey key = refreshToken ? refreshKey : accessKey;

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenAge() {
        return accessTokenAge;
    }

    public long getRefreshTokenAge() {
        return refreshTokenAge;
    }
}
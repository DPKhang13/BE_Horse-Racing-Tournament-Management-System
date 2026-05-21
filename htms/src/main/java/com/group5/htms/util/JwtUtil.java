package com.group5.htms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Builder
public class JwtUtil {

    private final String accessKey;
    private final String refreshKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public String generateAccessToken(String username) {
        return buildToken(new HashMap<>(), username, accessExpiration, getSignInKey(false));
    }

    public String generateAccessToken(Map<String, Object> extraClaims, String username) {
        return buildToken(extraClaims, username, accessExpiration, getSignInKey(false));
    }

    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, refreshExpiration, getSignInKey(true));
    }

    private String buildToken(Map<String, Object> extraClaims, String username, long expiration, SecretKey key) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username, boolean isRefreshToken) {
        final String extractedUsername = extractUsername(token, isRefreshToken);
        return (extractedUsername.equals(username)) && !isTokenExpired(token, isRefreshToken);
    }

    public String extractUsername(String token, boolean isRefreshToken) {
        return extractClaim(token, Claims::getSubject, isRefreshToken);
    }

    private boolean isTokenExpired(String token, boolean isRefreshToken) {
        return extractExpiration(token, isRefreshToken).before(new Date());
    }

    private Date extractExpiration(String token, boolean isRefreshToken) {
        return extractClaim(token, Claims::getExpiration, isRefreshToken);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean isRefreshToken) {
        final Claims claims = extractAllClaims(token, isRefreshToken);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, boolean isRefreshToken) {
        return Jwts.parser()
                .verifyWith(getSignInKey(isRefreshToken))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey(boolean isRefreshToken) {
        String secretString = isRefreshToken ? refreshKey : accessKey;
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

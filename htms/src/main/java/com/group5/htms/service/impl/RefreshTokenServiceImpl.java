package com.group5.htms.service.impl;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenServiceImpl {
    /*
      Lưu refresh token tạm thời trong RAM.

      Key   = jti của refresh token
      Value = username + hashed token + thời gian hết hạn
      - Server restart thì map này mất hết.
      - Phù hợp cho project/demo.
     - Production thì nên chuyển sang DB hoặc Redis.
     */
    private final Map<String, RefreshTokenData> refreshTokenMap = new ConcurrentHashMap<>();

    /*
     Lưu refresh token sau khi login/register/refresh-token.
     Không lưu raw token, chỉ lưu hash để an toàn hơn.
     */
    public void save(String jti, String rawToken, String username, long ttlMillis) {
        RefreshTokenData data = new RefreshTokenData(
                username,
                hashToken(rawToken),
                Instant.now().plusMillis(ttlMillis)
        );

        refreshTokenMap.put(jti, data);
    }

    /*
     Kiểm tra refresh token còn tồn tại, chưa hết hạn,
     đúng username và đúng hash token hay không.
     */
    public boolean isValid(String jti, String rawToken, String username) {
        RefreshTokenData data = refreshTokenMap.get(jti);

        if (data == null) {
            return false;
        }

        if (data.expiresAt().isBefore(Instant.now())) {
            refreshTokenMap.remove(jti);
            return false;
        }

        return data.username().equals(username)
                && data.tokenHash().equals(hashToken(rawToken));
    }

    public void revoke(String jti) {
        refreshTokenMap.remove(jti);
    }

    public void revokeAllByUsername(String username) {
        refreshTokenMap.entrySet()
                .removeIf(entry -> entry.getValue().username().equals(username));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash refresh token", ex);
        }
    }

    private record RefreshTokenData(
            String username,
            String tokenHash,
            Instant expiresAt
    ) {
    }
}
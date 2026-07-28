package com.group5.htms.dto.auth.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration in seconds.
     */
    private long expiresIn;

    private UserMeResponse user;
}
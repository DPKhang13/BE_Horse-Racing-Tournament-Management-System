package com.group5.htms.config;

import com.group5.htms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${JWT_ACCESSKEY}")
    private String accessKey;

    @Value("${JWT_REFRESHKEY}")
    private String refreshKey;

    @Value("${JWT_ACCESSEXPIRATION}")
    private long accessExpiration;

    @Value("${JWT_REFRESHEXPIRATION}")
    private long refreshExpiration;

    @Bean
    public JwtUtil jwtUtil() {
        return JwtUtil.builder()
                .accessKey(accessKey)
                .refreshKey(refreshKey)
                .accessExpiration(accessExpiration)
                .refreshExpiration(refreshExpiration)
                .build();
    }
}

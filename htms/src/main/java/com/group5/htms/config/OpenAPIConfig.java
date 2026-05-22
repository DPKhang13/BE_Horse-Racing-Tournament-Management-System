package com.group5.htms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI htmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Horse Racing Tournament Management System API")
                        .version("v1")
                        .description("API documentation for HTMS - Horse Racing Tournament Management System"))

                /*
                 * Server URL.
                 * "/" nghĩa là dùng chính backend host hiện tại.
                 * Ví dụ: http://localhost:8080
                 */
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default server")
                ))

                /*
                 * Khai báo JWT Bearer scheme để Swagger UI hiện nút Authorize.
                 */
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )

                /*
                 * Apply bearerAuth global cho API.
                 * Khi bấm Authorize trong Swagger, các API secured sẽ gửi Authorization header.
                 */
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}
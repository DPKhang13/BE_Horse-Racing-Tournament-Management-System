package com.group5.htms.config;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI htmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HTMS API")
                        .version("v1")
                        .description("Horse Racing Tournament Management System API"))
                .servers(List.of(new Server().url("/")
        ));
    }
}

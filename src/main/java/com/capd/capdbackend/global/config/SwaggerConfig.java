package com.capd.capdbackend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        // 서버 설정
        Server localServer = new Server();
        localServer.url(contextPath);
        localServer.setDescription("Local Server");

        // JWT 보안 스키마 설정 (자물쇠 버튼)
        String jwt = "JWT";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 모든 API에 자물쇠 적용
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        return new OpenAPI()
                .addServersItem(localServer)
                .components(new Components().addSecuritySchemes(jwt, securityScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info().title("Swagger API 명세서").version("1.0").description("게시판 프로젝트"));
    }

    @Bean
    public GroupedOpenApi customGroupedOpenApi() {
        return GroupedOpenApi.builder().group("api").pathsToMatch("/**").build();
    }
}

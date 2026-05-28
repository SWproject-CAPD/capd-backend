package com.capd.capdbackend.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "Refresh Token 발급 response dto", description = "Refresh Token 발급할때 서버에서 보내는 데이터")
public class RefreshTokenResponse {

    @Schema(description = "새로운 Access Token")
    private String accessToken;

    @JsonIgnore
    @Schema(description = "새로운 리프레시 토큰")
    private String refreshToken;

    @Schema(description = "Access Token 만료 시간")
    private Long expirationTime;
}

package com.capd.capdbackend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Refresh Token 발급 request dto", description = "Access Token이 만료되고 Refresh Token 발급할때 서버에게 요청 보내는 데이터")
public class RefreshTokenRequest {

    @Schema(description = "Refresh Token")
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
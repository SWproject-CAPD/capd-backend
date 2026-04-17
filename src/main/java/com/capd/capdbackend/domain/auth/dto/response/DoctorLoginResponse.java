package com.capd.capdbackend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "로그인 response dto", description = "사용자가 로그인할때 응답하는 데이터")
public class DoctorLoginResponse {

    @Schema(description = "사용자 발급 토큰")
    private String accessToken; // 사용자 토큰

    @Schema(description = "사용자 고유번호 id", example = "1")
    private Long userId;

    @Schema(description = "의사 고유번호 id", example = "1")
    private Long doctorId;

    @Schema(description = "사용자 이름", example = "김정모")
    private String name;

    @Schema(description = "사용자 토큰 만료시간", example = "1000000")
    private Long expiresAt;
}

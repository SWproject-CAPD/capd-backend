package com.capd.capdbackend.domain.doctor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "의사 본인 정보 조회 response dto", description = "의사 본인의 정보 조회할때 응답하는 데이터")
public class DoctorInfoResponse {

    @Schema(description = "사용자 고유 id", example = "1")
    private Long userId;

    @Schema(description = "의사 고유 id", example = "1")
    private Long doctorId;

    @Schema(description = "의사 이름", example = "김정모")
    private String name;

    @Schema(description = "의사 이메일", example = "kjm02@naver.com")
    private String email;

    @Schema(description = "의사 전화번호", example = "010-1111-1111")
    private String phone;

    @Schema(description = "회원가입 날짜", example = "2026-04-20T13:40:00")
    private LocalDateTime createdAt;

    @Schema(description = "회원정보 수정 날짜", example = "2026-04-20T13:40:00")
    private LocalDateTime updatedAt;
}

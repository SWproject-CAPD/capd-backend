package com.capd.capdbackend.domain.user.dto.response;

import com.capd.capdbackend.domain.user.entity.Gender;
import com.capd.capdbackend.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "회원가입 응답 dto", description = "회원가입할때 반환하는 데이터")
public class SignUpResponse {

    @Schema(description = "사용자 고유 id", example = "1")
    private Long userId;

    @Schema(description = "사용자 이메일", example = "kjm02@naver.com")
    private String email;

    @Schema(description = "사용자 이름", example = "김정모")
    private String name;

    @Schema(description = "사용자 생년월일", example = "2002-10-28")
    private LocalDate birthdate;

    @Schema(description = "사용자 전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "사용자 성별", example = "MAN")
    private Gender gender;

    @Schema(description = "사용자 권한", example = "DOCTOR")
    private Role role;

    @Schema(description = "회원가입 날짜", example = "2026-04-14T13:40:00")
    private LocalDateTime createdAt;
}

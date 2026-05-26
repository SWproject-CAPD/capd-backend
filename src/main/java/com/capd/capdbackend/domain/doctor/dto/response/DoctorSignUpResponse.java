package com.capd.capdbackend.domain.doctor.dto.response;

import com.capd.capdbackend.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "의사 회원가입 응답 dto", description = "의사가 회원가입할때 서버가 반환하는 데이터")
public class DoctorSignUpResponse {

    @Schema(description = "의사 고유번호", example = "1")
    private Long doctorId;

    @Schema(description = "사용자 고유번호", example = "1")
    private Long userId;

    @Schema(description = "의사 면허번호", example = "123456")
    private String licenseId;

    @Schema(description = "의사 이름", example = "김정모")
    private String userName;

    @Schema(description = "의사 이메일", example = "kjm02@naver.com")
    private String email;

    @Schema(description = "의사 전화번호", example = "010-1111-2222")
    private String phone;

    @Schema(description = "회원가입 날짜", example = "2026-04-16T13:40:00")
    private LocalDateTime createdAt;

    @Schema(description = "권한", example = "DOCTOR")
    private Role role;
}

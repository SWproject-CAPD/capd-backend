package com.capd.capdbackend.domain.patient.dto.response;

import com.capd.capdbackend.domain.patient.entity.Sex;
import com.capd.capdbackend.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "환자 본인 정보 조회 response dto", description = "환자 본인의 정보 조회할때 응답하는 데이터")
public class PatientInfoResponse {

    @Schema(description = "사용자 고유번호", example = "1")
    private Long userId;

    @Schema(description = "환자 고유번호", example = "1")
    private Long patientId;

    @Schema(description = "환자 이름", example = "배재훈")
    private String name;

    @Schema(description = "이메일", example = "bjh01@naver.com")
    private String email;

    @Schema(description = "전화번호", example = "010-2222-2222")
    private String phone;

    @Schema(description = "성별", example = "M")
    private Sex sex;

    @Schema(description = "나이", example = "26")
    private int age;

    @Schema(description = "사용자 권한", example = "PATIENT")
    private Role role;

    @Schema(description = "회원가입 날짜", example = "2026-04-17T13:40:00")
    private LocalDateTime createdAt;

    @Schema(description = "회원정보 수정 날짜", example = "2026-04-20T13:40:00")
    private LocalDateTime updatedAt;
}

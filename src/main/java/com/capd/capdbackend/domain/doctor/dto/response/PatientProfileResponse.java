package com.capd.capdbackend.domain.doctor.dto.response;

import com.capd.capdbackend.domain.patient.entity.Sex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "담당 환자 프로필 응답 dto", description = "의사가 담당 환자의 프로필을 볼때 서버에서 반환하는 데이터")
public class PatientProfileResponse {

    @Schema(description = "환자 고유번호", example = "1")
    private Long patientId;

    @Schema(description = "사용자 고유번호", example = "1")
    private Long userId;

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
}

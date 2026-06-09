package com.capd.capdbackend.domain.doctor.dto.response;

import com.capd.capdbackend.domain.patient.entity.Sex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "전화번호로 환자 조회 응답 dto", description = "환자의 전화번호를 이용해 환자를 조회하는 API")
public class PatientPhoneSearchResponse {

    @Schema(description = "환자 고유번호", example = "1")
    private Long patientId;

    @Schema(description = "환자 이름", example = "배재훈")
    private String name;

    @Schema(description = "전화번호", example = "010-2222-2222")
    private String phone;

    @Schema(description = "성별", example = "M")
    private Sex sex;

    @Schema(description = "나이", example = "23")
    private int age;
}

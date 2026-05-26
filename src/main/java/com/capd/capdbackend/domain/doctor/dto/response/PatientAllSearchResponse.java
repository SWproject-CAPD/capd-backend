package com.capd.capdbackend.domain.doctor.dto.response;

import com.capd.capdbackend.domain.patient.entity.Sex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "본인의 환자 전체 검색 응답 dto", description = "의사가 본인의 환자를 전체 검색할때 서버에서 반환하는 데이터")
public class PatientAllSearchResponse {

    @Schema(description = "환자 고유번호", example = "1")
    private Long patientId;

    @Schema(description = "사용자 고유번호", example = "1")
    private Long userId;

    @Schema(description = "환자 이름", example = "배재훈")
    private String name;

    @Schema(description = "성별", example = "M")
    private Sex sex;

    @Schema(description = "나이", example = "26")
    private int age;
}

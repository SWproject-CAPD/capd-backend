package com.capd.capdbackend.domain.capd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(title = "공통 투석일지 request dto", description = "환자의 하루 투석일지를 작성할때 서버에 요청 보내는 데이터")
@Setter
@Builder
@AllArgsConstructor
public class CapdCommonCreateRequest {

    @Schema(description = "투석 날짜", example = "2026-04-21")
    private LocalDate date;

    @Schema(description = "배액 혼탁 여부", example = "false")
    private boolean cloudyDialysate;

    @Schema(description = "하루 소변 횟수", example = "3")
    private int urinationCount;

    @Schema(description = "체중 (kg)", example = "65.5")
    private float bodyWeight;

    @Schema(description = "수축기 혈압 (높은 수치)", example = "120")
    private int bloodPressureSys;

    @Schema(description = "이완기 혈압 (낮은 수치)", example = "80")
    private int bloodPressureDia;

    @Schema(description = "공복 혈당 (mg/dL)", example = "100.0")
    private float fastingBloodSugar;

    @Schema(description = "특이사항 메모", example = "오늘 컨디션이 좋습니다.")
    private String note;
}

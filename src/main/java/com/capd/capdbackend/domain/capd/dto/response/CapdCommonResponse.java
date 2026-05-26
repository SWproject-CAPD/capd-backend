package com.capd.capdbackend.domain.capd.dto.response;

import com.capd.capdbackend.domain.capd.entity.CapdStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "투석일지 응답 dto", description = "환자가 투석일지를 제출할때 서버가 반환하는 데이터")
public class CapdCommonResponse {

    // 공통 투석 일지
    @Schema(description = "투석일지 고유번호", example = "1")
    private Long capdId;

    @Schema(description = "투석 날짜", example = "2026-04-21")
    private LocalDate date;

    @Schema(description = "배액 혼탁 여부", example = "false")
    private boolean cloudyDialysate;

    @Schema(description = "하루 소변 횟수", example = "3")
    private int urinationCount;

    @Schema(description = "총초여과량 (총제수량)", example = "0.5")
    private float totalUltrafiltration;

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

    @Schema(description = "상태 (TEMP / SUBMITTED", example = "TEMP")
    private CapdStatus status;

    @Schema(description = "세션 리스트", example = "1회차, 2회차...")
    private List<CapdSessionResponse> sessions;
}

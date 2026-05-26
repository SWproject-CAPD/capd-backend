package com.capd.capdbackend.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "주간 보고서 응답 dto", description = "주간 보고서를 생성할때 서버에서 반환하는 데이터")
public class ReportCreateResponse {

    @Schema(description = "보고서 고유번호", example = "1")
    private Long reportId;

    @Schema(description = "의사 ID", example = "1")
    private Long doctorId;

    @Schema(description = "의사 이름", example = "김정모")
    private String doctorName;

    @Schema(description = "환자 ID", example = "2")
    private Long patientId;

    @Schema(description = "환자 이름", example = "배재훈")
    private String patientName;

    @Schema(description = "보고서 시작일", example = "2026-04-30")
    private LocalDate startDate;

    @Schema(description = "보고서 종료일", example = "2026-05-06")
    private LocalDate endDate;

    @Schema(description = "체중 변화 요약")
    private String weightSummary;

    @Schema(description = "혈압 변화 요약")
    private String bpSummary;

    @Schema(description = "혈당 변화 요약")
    private String bloodSugarSummary;

    @Schema(description = "총초여과량 변화 요약")
    private String ufSummary;

    @Schema(description = "이상치 발생 요약")
    private String anomalySummary;

    @Schema(description = "AI 종합 소견")
    private String docSummary;

    @Schema(description = "PDF 저장 경로")
    private String docSaveLocation;
}

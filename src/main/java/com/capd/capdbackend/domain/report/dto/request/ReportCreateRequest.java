package com.capd.capdbackend.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "주간 보고서 생성 request dto", description = "의사가 월과 주차를 선택해서 보고서를 생성할 때 보내는 데이터")
public class ReportCreateRequest {

    @Schema(description = "연도", example = "2026")
    @NotNull(message = "연도는 필수입니다.")
    private Integer year;

    @Schema(description = "월 (1~12)", example = "5")
    @NotNull(message = "월은 필수입니다.")
    @Min(value = 1, message = "월은 1 이상이어야 합니다.")
    @Max(value = 12, message = "월은 12 이하이어야 합니다.")
    private Integer month;

    @Schema(description = "주차 (1~5)", example = "2")
    @NotNull(message = "주차는 필수입니다.")
    @Min(value = 1, message = "주차는 1 이상이어야 합니다.")
    @Max(value = 5, message = "주차는 5 이하이어야 합니다.")
    private Integer weekNumber;
}

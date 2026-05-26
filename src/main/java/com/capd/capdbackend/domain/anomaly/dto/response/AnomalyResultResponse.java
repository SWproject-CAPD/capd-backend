package com.capd.capdbackend.domain.anomaly.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "이상치 분석 결과 응답 dto", description = "AI 이상치 분석 결과를 반환하는 데이터")
public class AnomalyResultResponse {

    @Schema(description = "분석 결과 고유번호", example = "1")
    private Long anomalyId;

    @Schema(description = "분석 날짜", example = "2026-04-29")
    private LocalDate analysisDate;

    @Schema(description = "위험 단계 (1=정상, 2=주의, 3=위험)", example = "1")
    private int riskLevel;

    @Schema(description = "이상치 점수 (높을수록 정상)", example = "-0.032")
    private float anomalyScore;

    @Schema(description = "상태 메시지", example = "주의 (Warning) - 관심이 필요합니다.")
    private String statusMessage;

    @Schema(description = "이상치 원인 Top 3 (JSON 문자열)", example = "[{\"feature\":\"체중\",\"direction\":\"급상승\",\"impact_score\":1.23}]")
    private String topCauses;
}

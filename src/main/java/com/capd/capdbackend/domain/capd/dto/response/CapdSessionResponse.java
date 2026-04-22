package com.capd.capdbackend.domain.capd.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "세션 투석일지 응답 dto", description = "환자가 세션 투석일지를 제출할때 서버가 반환하는 데이터")
public class CapdSessionResponse {

    @Schema(description = "세션 투석일지 고유번호", example = "1")
    private Long capdSessionId;

    @Schema(description = "교환 회차 (1~5)", example = "1")
    private int sessionNumber;

    @Schema(description = "교환 시작 시간", example = "10:30:00")
    private LocalTime exchangeTime;

    @Schema(description = "배액량(g)", example = "2200.5")
    private float drainVolume;

    @Schema(description = "포도당 농도 (1.5%, 2.5%, 4.25% 중에서 선택)", example = "1.5")
    private float dialysateConcentration;

    @Schema(description = "주입액 무게(g)", example = "2000.0")
    private float infusedFluidWeight;

    @Schema(description = "초여과량 (제수량)", example = "0.5")
    private float ultrafiltration;
}

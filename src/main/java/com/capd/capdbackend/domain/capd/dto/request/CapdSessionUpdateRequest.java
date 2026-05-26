package com.capd.capdbackend.domain.capd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Schema(title = "세션 투석일지 수정 request dto", description = "환자의 세션 투석일지를 수정할때 서버에 요청 보내는 데이터")
@Setter
@Builder
@AllArgsConstructor
public class CapdSessionUpdateRequest {

    @Schema(description = "교환 시작 시간", example = "10:30:00")
    private LocalTime exchangeTime;

    @Schema(description = "배액량 (g)", example = "2200.5")
    private float drainVolume;

    @Schema(description = "포도당 농도 (1.5 / 2.5 / 4.25)", example = "1.5")
    private float dialysateConcentration;

    @Schema(description = "주입액 무게 (g)", example = "2000.0")
    private float infusedFluidWeight;
}

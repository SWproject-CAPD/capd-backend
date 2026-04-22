package com.capd.capdbackend.domain.capd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Schema(title = "투석일지 세션 request dto", description = "환자가 투석일지 n회차 작성할때마다 서버에 요청 보내는 데이터")
@Setter
public class CapdSessionCreateRequest {

    @Schema(description = "투석 날짜", example = "2026-04-21")
    @NotNull(message = "투석 날짜는 필수 입력값입니다.")
    private LocalDate date;

    @Schema(description = "교환 회차 (1~5)", example = "1")
    @Min(value = 1, message = "세션 번호는 1 이상이어야 합니다.")
    @Max(value = 5, message = "세션 번호는 5 이하이어야 합니다.")
    private Integer sessionNumber;

    @Schema(description = "교환 시작 시간", example = "10:30:00")
    private LocalTime exchangeTime;

    @Schema(description = "배액량(g)", example = "2200.5")
    private float drainVolume;

    @Schema(description = "포도당 농도 (1.5%, 2.5%, 4.25% 중에서 선택)", example = "1.5")
    private float dialysateConcentration;

    @Schema(description = "주입액 무게(g)", example = "2000.0")
    private float infusedFluidWeight;
}

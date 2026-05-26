package com.capd.capdbackend.domain.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "환자 진료 예약 조회 response dto", description = "환자가 본인의 진료 예약 정보 조회할때 반환하는 데이터")
public class ReservationPatientReadResponse {

    @Schema(description = "예약 고유번호", example = "1")
    private Long reservationId;

    @Schema(description = "담당 의사 이름", example = "김정모")
    private String doctorName;

    @Schema(description = "예약 날짜 및 시간", example = "2026-05-06T14:00:00")
    private LocalDateTime reservationDate;

}

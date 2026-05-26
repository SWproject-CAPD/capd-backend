package com.capd.capdbackend.domain.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "의사 진료 예약 조회 response dto", description = "의사가 특정 날짜에 담당 환자 진료 예약 정보 조회할때 반환하는 데이터")
public class ReservationDoctorReadResponse {

    @Schema(description = "진료 예약 고유번호", example = "1")
    private Long reservationId;

    @Schema(description = "환자 ID", example = "1")
    private Long patientId;

    @Schema(description = "환자 이름", example = "배재훈")
    private String patientName;

    @Schema(description = "환자 전화번호", example = "010-2222-2222")
    private String phone;

    @Schema(description = "예약 날짜 및 시간", example = "2026-05-06T14:00:00")
    private LocalDateTime reservationDate;
}

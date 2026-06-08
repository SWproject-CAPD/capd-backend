package com.capd.capdbackend.domain.reservation.dto.response;

import com.capd.capdbackend.domain.reservation.entity.ReservationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "진료 예약 response dto", description = "진료 예약 정보를 반환하는 데이터")
public class ReservationCreateResponse {

    @Schema(description = "예약 고유번호", example = "1")
    private Long reservationId;

    @Schema(description = "환자 ID", example = "1")
    private Long patientId;

    @Schema(description = "환자 이름", example = "배재훈")
    private String patientName;

    @Schema(description = "환자 전화번호", example = "010-2222-2222")
    private String phone;

    @Schema(description = "의사 ID", example = "1")
    private Long doctorId;

    @Schema(description = "의사 이름", example = "김정모")
    private String doctorName;

    @Schema(description = "예약 날짜 및 시간", example = "2026-05-06T14:00:00")
    private LocalDateTime reservationDate;

    @Schema(description = "예약 유형", example = "REGULAR_CHECKUP")
    private ReservationType type;
}

package com.capd.capdbackend.domain.reservation.dto.request;

import com.capd.capdbackend.domain.reservation.entity.ReservationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "진료 예약 생성 request dto", description = "의사가 환자 진료 예약을 생성할 때 보내는 데이터")
public class ReservationCreateRequest {

    @Schema(description = "환자 고유번호", example = "1")
    @NotNull(message = "환자 전화번호는 필수 값입니다.")
    private Long patientId;

    @Schema(description = "예약 날짜 및 시간", example = "2026-05-06T09:00:00")
    @NotNull(message = "예약 날짜는 필수입니다.")
    private LocalDateTime reservationDate;

    @Schema(description = "예약 유형", example = "REGULAR_CHECKUP")
    @NotNull(message = "예약 유형은 필수입니다.")
    private ReservationType type;
}

package com.capd.capdbackend.domain.reservation.controller;

import com.capd.capdbackend.domain.reservation.dto.request.ReservationCreateRequest;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationCreateResponse;
import com.capd.capdbackend.domain.reservation.service.ReservationService;
import com.capd.capdbackend.global.response.BaseResponse;
import com.capd.capdbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Reservation", description = "진료 예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    // 진료 예약 생성
    @Operation(summary = "진료 예약 생성", description = "의사가 환자 id를 가지고 진료 예약 시간을 생성하는 API")
    @PostMapping("/reservations")
    public ResponseEntity<BaseResponse<ReservationCreateResponse>> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReservationCreateRequest request) {

        // service 호출
        ReservationCreateResponse response = reservationService.reservationCreate(userDetails.getIdentifier(), request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "진료 예약 생성 성공", response));
    }
}

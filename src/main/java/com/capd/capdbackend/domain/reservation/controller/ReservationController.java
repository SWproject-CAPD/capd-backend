package com.capd.capdbackend.domain.reservation.controller;

import com.capd.capdbackend.domain.reservation.dto.request.ReservationCreateRequest;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationCreateResponse;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationDoctorReadResponse;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationPatientReadResponse;
import com.capd.capdbackend.domain.reservation.service.ReservationService;
import com.capd.capdbackend.global.response.BaseResponse;
import com.capd.capdbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    // 환자가 진료 예약 조회
    @Operation(summary = "환자 본인 진료 예약 조회", description = "환자가 본인의 진료 예약 날짜를 최신순으로 조회하는 API")
    @GetMapping("/reservations/patient")
    public ResponseEntity<BaseResponse<List<ReservationPatientReadResponse>>> readPatientReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        List<ReservationPatientReadResponse> response = reservationService.patientReservation(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "진료 예약 조회 성공", response));
    }

    // 의사가 특정 날짜 진료 예약 조회
    @Operation(summary = "의사 날짜별 진료 예약 조회", description = "의사가 특정 날짜에서 진료 예약을 오름차순으로 조회하는 API")
    @GetMapping("/reservations/doctor/date")
    public ResponseEntity<BaseResponse<List<ReservationDoctorReadResponse>>> readDoctorReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // service 호출
        List<ReservationDoctorReadResponse> response = reservationService.doctorReservation(userDetails.getIdentifier(), date);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "진료 예약 조회 성공", response));
    }
}

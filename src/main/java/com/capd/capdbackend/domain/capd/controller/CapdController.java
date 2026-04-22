package com.capd.capdbackend.domain.capd.controller;

import com.capd.capdbackend.domain.capd.dto.request.CapdCommonCreateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdSessionCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.service.CapdService;
import com.capd.capdbackend.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Capd", description = "투석일지 관련 API")
public class CapdController {

    private final CapdService capdService;

    // 공통 투석일지 제출 API
    @Operation(summary = "공통 투석일지 제출", description = "그날의 체중, 혈당, 메모 등 공통 정보를 제출하는 API")
    @PostMapping("/capds/commons/{patient-id}")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> capdCommon(
            @RequestBody @Valid CapdCommonCreateRequest request,
            @PathVariable("patient-id") Long patientId) {

        // service 호출
        CapdCommonResponse capdCommonCreateResponse = capdService.createCommonCapd(patientId, request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "공통 투석일지 제출 성공", capdCommonCreateResponse));
    }

    // 세션 투석일지 제출 API
    @Operation(summary = "세션 투석일지 제출", description = "공통 투석일지 정보를 제외한 세션 투석일지 정보를 제출하는 API (1~5회차 제출로 구성)")
    @PostMapping("/capds/sessions/{patient-id}")
    public ResponseEntity<BaseResponse<CapdSessionResponse>> capdSession(
            @RequestBody @Valid CapdSessionCreateRequest request,
            @PathVariable("patient-id") Long patientId) {

        // service 호출
        CapdSessionResponse capdSessionCreateResponse = capdService.createSessionCapd(patientId, request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "세션 투석일지 제출 성공", capdSessionCreateResponse));
    }

    // 전체 투석일지 조회
    @Operation(summary = "전체 투석일지 조회", description = "환자가 작성한 모든 투석일지를 최신순으로 조회하는 API")
    @GetMapping("/capds/commons/{patient-id}")
    public ResponseEntity<BaseResponse<List<CapdCommonResponse>>> capdAllRead(
            @PathVariable("patient-id") Long patientId) {

        // service 호출
        List<CapdCommonResponse> response = capdService.capdAllRead(patientId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "전체 투석일지 조회 성공", response));
    }

    // 당일 투석일지 조회
    @Operation(summary = "특정 날짜 투석일지 조회", description = "해당 날짜의 공통 정보와 세션 목록을 함께 조회하는 API")
    @GetMapping("/capds/commons/{patient-id}/date")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> capdDateCommonRead(
            @PathVariable("patient-id") Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // service 호출
        CapdCommonResponse response = capdService.capdProfileRead(patientId, date);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "특정 날짜 투석일지 조회 성공", response));
    }

    // 특정 세션 투석일지 조회
    @Operation(summary = "특정 세션 투석일지 조회", description = "해당 날짜의 특정 회차(1~5) 세션 정보만 단건으로 조회하는 API")
    @GetMapping("/capds/sessions/{patient-id}/search")
    public ResponseEntity<BaseResponse<CapdSessionResponse>> capdDateSessionRead(
            @PathVariable("patient-id") Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int sessionNumber) {

        // service 호출
        CapdSessionResponse response = capdService.capdSessionRead(patientId, date, sessionNumber);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "특정 세션 투석일지 조회 성공", response));
    }

    // ID로 투석일지 조회
    @Operation(summary = "공통 투석일지 단건 상세 조회", description = "목록에서 일지를 클릭했을 때 ID로 투석일지를 조회하는 API")
    @GetMapping("/capds/commons/{patient-id}/{capd-id}")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> capdCommonIdRead(
            @PathVariable("patient-id") Long patientId,
            @PathVariable("capd-id") Long capdId) {

        // service 호출
        CapdCommonResponse response = capdService.capdIDCommonRead(patientId, capdId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "ID로 투석일지 상세 조회 성공", response));
    }

    // ID로 세션 투석일지 조회
    @Operation(summary = "세션 투석일지 단건 상세 조회", description = "세션 ID로 특정 세션 투석일지를 단건 조회하는 API")
    @GetMapping("/capds/sessions/{patient-id}/{session-id}")
    public ResponseEntity<BaseResponse<CapdSessionResponse>> capdSessionIdRead(
            @PathVariable("patient-id") Long patientId,
            @PathVariable("session-id") Long capdSessionId) {

        // service 호출
        CapdSessionResponse response = capdService.capdSessionIdRead(patientId, capdSessionId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "ID로 세션 투석일지 단건 조회 성공", response));
    }
}

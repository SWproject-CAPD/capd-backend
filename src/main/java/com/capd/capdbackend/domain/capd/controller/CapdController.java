package com.capd.capdbackend.domain.capd.controller;

import com.capd.capdbackend.domain.capd.dto.request.CapdCreateRequest;
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

    // 임시저장 (공통 + 세션 같이 or 세션만)
    @Operation(summary = "투석일지 임시저장",
            description = "처음엔 공통 정보 + 세션 같이, 이후엔 세션만 추가해서 호출. 공통 정보는 매번 같이 보내도 됨")
    @PostMapping("/capds/{patientId}/temp")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> saveCapd(
            @PathVariable Long patientId,
            @RequestBody @Valid CapdCreateRequest request) {

        CapdCommonResponse response = capdService.saveCapd(patientId, request);
        return ResponseEntity.ok(BaseResponse.success(200, "임시저장 성공", response));
    }

    // 임시저장 데이터 불러오기
    @Operation(summary = "임시저장 데이터 조회",
            description = "페이지 진입 시 당일 임시저장 데이터 불러오기")
    @GetMapping("/capds/{patientId}/temp")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> getTempCapd(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        CapdCommonResponse response = capdService.getTempCapd(patientId, date);
        return ResponseEntity.ok(BaseResponse.success(200, "임시저장 데이터 조회 성공", response));
    }

    // 최종 제출 (마감하기)
    @Operation(summary = "투석일지 최종 제출",
            description = "마감하기 버튼 — 공통 + 세션 전체 한 번에 제출")
    @PostMapping("/capds/{patientId}")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> submitCapd(
            @PathVariable Long patientId,
            @RequestBody @Valid CapdCreateRequest request) {

        CapdCommonResponse response = capdService.submitCapd(patientId, request);
        return ResponseEntity.ok(BaseResponse.success(201, "투석일지 최종 제출 성공", response));
    }

    // 전체 목록 조회 (환자용)
    @Operation(summary = "전체 투석일지 조회",
            description = "환자 본인의 전체 투석일지 최신순 조회 (임시저장 포함)")
    @GetMapping("/capds/{patientId}")
    public ResponseEntity<BaseResponse<List<CapdCommonResponse>>> capdAllRead(
            @PathVariable Long patientId) {

        List<CapdCommonResponse> response = capdService.capdAllReadForPatient(patientId);
        return ResponseEntity.ok(BaseResponse.success(200, "전체 투석일지 조회 성공", response));
    }

    // 날짜로 단건 조회
    @Operation(summary = "날짜로 투석일지 조회",
            description = "특정 날짜의 공통 기록 + 세션 목록 조회")
    @GetMapping("/capds/{patientId}/date")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> capdDateRead(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        CapdCommonResponse response = capdService.capdProfileRead(patientId, date);
        return ResponseEntity.ok(BaseResponse.success(200, "날짜로 투석일지 조회 성공", response));
    }

    // ID로 단건 조회
    @Operation(summary = "ID로 투석일지 조회",
            description = "공통 투석일지 ID로 단건 조회")
    @GetMapping("/capds/{patientId}/{capdId}")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> capdIdRead(
            @PathVariable Long patientId,
            @PathVariable Long capdId) {

        CapdCommonResponse response = capdService.capdIDCommonRead(patientId, capdId);
        return ResponseEntity.ok(BaseResponse.success(200, "ID로 투석일지 조회 성공", response));
    }

    // 날짜 + 회차로 특정 세션 조회
    @Operation(summary = "특정 세션 조회",
            description = "날짜 + 회차 번호로 특정 세션 단건 조회")
    @GetMapping("/capds/{patientId}/sessions")
    public ResponseEntity<BaseResponse<CapdSessionResponse>> capdSessionRead(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int sessionNumber) {

        CapdSessionResponse response = capdService.capdSessionRead(patientId, date, sessionNumber);
        return ResponseEntity.ok(BaseResponse.success(200, "특정 세션 조회 성공", response));
    }

    // 세션 ID로 단건 조회
    @Operation(summary = "세션 ID로 조회",
            description = "세션 ID로 특정 세션 단건 조회")
    @GetMapping("/capds/{patientId}/sessions/{sessionId}")
    public ResponseEntity<BaseResponse<CapdSessionResponse>> capdSessionIdRead(
            @PathVariable Long patientId,
            @PathVariable Long sessionId) {

        CapdSessionResponse response = capdService.capdSessionIdRead(patientId, sessionId);
        return ResponseEntity.ok(BaseResponse.success(200, "세션 ID로 조회 성공", response));
    }
}
package com.capd.capdbackend.domain.capd.controller;

import com.capd.capdbackend.domain.capd.dto.request.CapdCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.service.CapdService;
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
@Tag(name = "Capd", description = "투석일지 관련 API")
public class CapdController {

    private final CapdService capdService;

    // 임시저장 (공통 + 세션 같이 or 세션만)
    @Operation(summary = "투석일지 임시저장", description = "처음엔 공통 정보 + 세션 같이, 이후엔 세션만 추가해서 호출. 공통 정보는 매번 같이 보내는 API")
    @PostMapping("/capds/temp")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> saveCapd(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CapdCreateRequest request) {

        // service 호출
        CapdCommonResponse response = capdService.saveCapd(userDetails.getIdentifier(), request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "임시저장 성공", response));
    }

    // 임시저장 데이터 불러오기
    @Operation(summary = "임시저장 데이터 조회", description = "페이지 진입 시 당일 임시저장한 투석일지 데이터 불러오는 API")
    @GetMapping("/capds/temp")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> getTempCapd(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // service 호출
        CapdCommonResponse response = capdService.getTempCapd(userDetails.getIdentifier(), date);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "임시저장 데이터 조회 성공", response));
    }

    // 최종 제출 (마감하기)
    @Operation(summary = "투석일지 최종 제출", description = "마감하기 버튼을 눌러서 공통 + 세션 전체 한 번에 제출하는 API")
    @PostMapping("/capds/submit")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> submitCapd(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CapdCreateRequest request) {

        // service 호출
        CapdCommonResponse response = capdService.submitCapd(userDetails.getIdentifier(), request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "투석일지 최종 제출 성공", response));
    }

    // 전체 목록 조회 (환자용)
    @Operation(summary = "전체 투석일지 조회", description = "환자 본인의 전체 투석일지 최신순 조회 (임시저장 포함)하는 API")
    @GetMapping("/capds")
    public ResponseEntity<BaseResponse<List<CapdCommonResponse>>> capdAllRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        List<CapdCommonResponse> response = capdService.capdAllReadForPatient(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "전체 투석일지 조회 성공", response));
    }

    // 날짜로 단건 조회
    @Operation(summary = "날짜로 투석일지 조회", description = "특정 날짜의 공통 기록 + 세션 목록 조회하는 API")
    @GetMapping("/capds/date")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> capdDateRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // service 호출
        CapdCommonResponse response = capdService.capdProfileRead(userDetails.getIdentifier(), date);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "날짜로 투석일지 조회 성공", response));
    }

    // ID로 단건 조회
    @Operation(summary = "ID로 투석일지 조회", description = "공통 투석일지 ID로 단건 조회하는 API")
    @GetMapping("/capds/{capdId}")
    public ResponseEntity<BaseResponse<CapdCommonResponse>> capdIdRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long capdId) {

        // service 호출
        CapdCommonResponse response = capdService.capdIDCommonRead(userDetails.getIdentifier(), capdId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "ID로 투석일지 조회 성공", response));
    }

    // 날짜 + 회차로 특정 세션 조회
    @Operation(summary = "특정 세션 조회", description = "날짜 + 회차 번호로 특정 세션 단건 조회하는 API")
    @GetMapping("/capds/sessions")
    public ResponseEntity<BaseResponse<CapdSessionResponse>> capdSessionRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int sessionNumber) {


        // service 호출
        CapdSessionResponse response = capdService.capdSessionRead(userDetails.getIdentifier(), date, sessionNumber);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "특정 세션 조회 성공", response));
    }

    // 세션 ID로 단건 조회
    @Operation(summary = "세션 ID로 조회", description = "세션 ID로 특정 세션 단건 조회하는 API")
    @GetMapping("/capds/sessions/{sessionId}")
    public ResponseEntity<BaseResponse<CapdSessionResponse>> capdSessionIdRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sessionId) {

        // service 호출
        CapdSessionResponse response = capdService.capdSessionIdRead(userDetails.getIdentifier(), sessionId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "세션 ID로 조회 성공", response));
    }

    // 세션 투석일지 삭제
    @Operation(summary = "특정 세션 삭제", description = "특정 회차의 세션 기록만 삭제하는 API")
    @DeleteMapping("/capds/sessions/{sessionId}")
    public ResponseEntity<BaseResponse<Void>> deleteCapdSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sessionId) {

        // service 호출
        capdService.deleteCapdSession(userDetails.getIdentifier(), sessionId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "해당 세션 투석일지 삭제 성공", null));
    }

    // 투석일제 삭제
    @Operation(summary = "전체 투석일지 삭제", description = "특정 투석일지(공통+세션 전체)를 삭제하는 API")
    @DeleteMapping("/capds/{capdId}")
    public ResponseEntity<BaseResponse<Void>> deleteCapdCommon(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long capdId) {

        // service 호출
        capdService.deleteCapdCommon(userDetails.getIdentifier(), capdId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "투석일지가 전체 삭제되었습니다.", null));
    }
}
package com.capd.capdbackend.domain.anomaly.controller;

import com.capd.capdbackend.domain.anomaly.dto.response.AnomalyResultResponse;
import com.capd.capdbackend.domain.anomaly.service.AnomalyService;
import com.capd.capdbackend.global.response.BaseResponse;
import com.capd.capdbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Anomaly", description = "이상치 분석 관련 API")
public class AnomalyController {

    private final AnomalyService anomalyService;

    // 저장된 전체 이상치 결과 조회
    @Operation(summary = "이상치 분석 결과 전체 조회 API", description = "저장된 전체 이상치 분석 결과 최신순 조회하는 API")
    @GetMapping("/anomaly/{patientId}")
    public ResponseEntity<BaseResponse<List<AnomalyResultResponse>>> getAnomalyResults(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long patientId) {

        // service 호출
        List<AnomalyResultResponse> results = anomalyService.getAnomalyResults(userDetails.getIdentifier(), patientId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "이상치 분석 결과 조회 성공", results));
    }

    // 의사가 특정 날짜 분석 직접 요청
    @Operation(summary = "이상치 분석 요청", description = "의사가 특정 날짜 투석 데이터를 AI로 분석 요청하는 API")
    @PostMapping("/anomaly/{patientId}/analyze")
    public ResponseEntity<BaseResponse<AnomalyResultResponse>> analyzePatient(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // service 호출
        AnomalyResultResponse result = anomalyService.analyzePatient(userDetails.getIdentifier(), patientId, date);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "이상치 분석 완료", result));
    }
}

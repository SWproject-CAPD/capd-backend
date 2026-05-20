package com.capd.capdbackend.domain.report.controller;

import com.capd.capdbackend.domain.report.dto.request.ReportCreateRequest;
import com.capd.capdbackend.domain.report.dto.response.ReportCreateResponse;
import com.capd.capdbackend.domain.report.service.ReportService;
import com.capd.capdbackend.global.response.BaseResponse;
import com.capd.capdbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Report", description = "주간 보고서 관련 API")
public class ReportController {

    private final ReportService reportService;

    // 월 + 주차 선택 보고서 생성
    @Operation(summary = "주간 보고서 생성", description = "의사가 연도, 월, 주차를 선택해서 보고서를 생성하는 API")
    @PostMapping("/reports/{patientId}")
    public ResponseEntity<BaseResponse<ReportCreateResponse>> generateReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long patientId,
            @RequestBody @Valid ReportCreateRequest request) {

        // service 호출
        ReportCreateResponse response = reportService.generateReport(userDetails.getIdentifier(), patientId, request.getYear(), request.getMonth(), request.getWeekNumber());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "주간 보고서 생성 성공", response));
    }

    // 저장된 보고서 전체 조회
    @Operation(summary = "주간 보고서 전체 조회", description = "저장된 전체 보고서 최신순 조회")
    @GetMapping("/reports/{patientId}")
    public ResponseEntity<BaseResponse<List<ReportCreateResponse>>> getReports(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long patientId) {

        // service 호출
        List<ReportCreateResponse> response = reportService.getReports(userDetails.getIdentifier(), patientId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "보고서 조회 성공", response));
    }

    // PDF 생성
    @Operation(summary = "주간 보고서 PDF 생성", description = "저장된 보고서를 PDF로 생성하는 API")
    @PostMapping("/reports/{reportId}/pdf")
    public ResponseEntity<BaseResponse<String>> createPdf(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId) {

        // service 호출
        String pdfUrl = reportService.createPdf(userDetails.getIdentifier(), reportId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "PDF 생성 성공", pdfUrl));
    }
}

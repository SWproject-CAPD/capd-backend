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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

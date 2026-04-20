package com.capd.capdbackend.domain.patient.controller;

import com.capd.capdbackend.domain.patient.dto.request.PatientSignUpRequest;
import com.capd.capdbackend.domain.patient.dto.response.PatientInfoResponse;
import com.capd.capdbackend.domain.patient.dto.response.PatientSignUpResponse;
import com.capd.capdbackend.domain.patient.service.PatientService;
import com.capd.capdbackend.global.response.BaseResponse;
import com.capd.capdbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Patient", description = "환자 관련 API")
public class PatientController {

    private final PatientService patientService;

    // 회원가입 API
    @Operation(summary = "회원가입", description = "환자가 이메일, 전화번호, 성별, 전화번호 등 데이터를 넣어 회원가입하는 API")
    @PostMapping("/patients")
    public ResponseEntity<BaseResponse<PatientSignUpResponse>> patientSignUp(
            @RequestBody @Valid PatientSignUpRequest request) {

        // service 호출
        PatientSignUpResponse patientSignUpResponse = patientService.patientSignUp(request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "회원가입 성공", patientSignUpResponse));
    }

    // 본인 정보 조회 API
    @Operation(summary = "환자 본인 정보 조회", description = "환자가 본인의 정보를 조회하는 API")
    @GetMapping("/patients/me")
    public ResponseEntity<BaseResponse<PatientInfoResponse>> patientInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        PatientInfoResponse patientInfo = patientService.patientInfo(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "환자 본인 정보 조회 성공", patientInfo));
    }

    // 환자 삭제 API
    @Operation(summary = "환자 사용자 삭제", description = "환자인 사용자가 탈퇴하는 API")
    @DeleteMapping("/patients/me")
    public ResponseEntity<BaseResponse<Void>> patientDelete(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        patientService.patientDelete(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "환자 사용자 삭제 성공", null));
    }
}

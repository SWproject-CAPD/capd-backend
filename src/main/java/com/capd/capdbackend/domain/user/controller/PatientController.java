package com.capd.capdbackend.domain.user.controller;

import com.capd.capdbackend.domain.user.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.user.dto.request.PatientSignUpRequest;
import com.capd.capdbackend.domain.user.dto.response.SignUpResponse;
import com.capd.capdbackend.domain.user.service.PatientService;
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

    // 회원가입 api
    @Operation(summary = "회원가입", description = "환자 회원가입 API")
    @PostMapping("/patients")
    public ResponseEntity<BaseResponse<SignUpResponse>> signUp(
            @RequestBody @Valid PatientSignUpRequest request) {

        // 서비스 호출
        SignUpResponse signUpResponse = patientService.signUp(request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "회원가입을 성공했습니다.", signUpResponse));
    }

    // 환자 삭제 api
    @Operation(summary = "회원탈퇴", description = "환자인 사용자를 삭제하는 API")
    @DeleteMapping("/patients")
    public ResponseEntity<BaseResponse<Void>> deletePatient(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 서비스 호출
        patientService.deletePatient(userDetails.getUsername());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "환자인 사용자 삭제 성공", null));
    }
}

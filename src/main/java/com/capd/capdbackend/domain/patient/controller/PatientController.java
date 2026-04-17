package com.capd.capdbackend.domain.patient.controller;

import com.capd.capdbackend.domain.patient.dto.request.PatientSignUpRequest;
import com.capd.capdbackend.domain.patient.dto.response.PatientSignUpResponse;
import com.capd.capdbackend.domain.patient.service.PatientService;
import com.capd.capdbackend.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

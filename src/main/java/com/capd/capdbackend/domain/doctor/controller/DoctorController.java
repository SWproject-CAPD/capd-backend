package com.capd.capdbackend.domain.doctor.controller;

import com.capd.capdbackend.domain.doctor.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.doctor.dto.request.PatientRegisterRequest;
import com.capd.capdbackend.domain.doctor.dto.response.*;
import com.capd.capdbackend.domain.doctor.service.DoctorService;
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
@Tag(name = "Doctor", description = "의사 관련 API")
public class DoctorController {

    private final DoctorService doctorService;

    // 회원가입 API
    @Operation(summary = "회원가입", description = "의사가 면허번호와 비밀번호 등 데이터를 넣어 회원가입 하는 API")
    @PostMapping("/doctors")
    public ResponseEntity<BaseResponse<DoctorSignUpResponse>> doctorSignUp(
            @RequestBody @Valid DoctorSignUpRequest request) {

        // service 호출
        DoctorSignUpResponse doctorSignUpResponse = doctorService.doctorSignUp(request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "회원가입 성공", doctorSignUpResponse));
    }

    // 본인 정보 조회 API
    @Operation(summary = "의사 본인 정보 조회", description = "의사가 본인의 정보 조회하는 API")
    @GetMapping("/doctors/me")
    public ResponseEntity<BaseResponse<DoctorInfoResponse>> doctorInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        DoctorInfoResponse doctorInfo = doctorService.doctorInfo(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "의사 본인 정보 조회 성공", doctorInfo));
    }

    // 의사 회원탈퇴 API
    @Operation(summary = "의사 사용자 삭제", description = "의사인 사용자 삭제하는 API")
    @DeleteMapping("/doctors/me")
    public ResponseEntity<BaseResponse<Void>> doctorDelete(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        doctorService.doctorDelete(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "의사 사용자 삭제 성공", null));
    }

    // 본인의 환자로 등록하는 API
    @Operation(summary = "환자 등록 API", description = "의사가 환자의 전화번호를 이용해 본인의 환자로 등록하는 API")
    @PostMapping("/doctors/patients")
    public ResponseEntity<BaseResponse<PatientRegisterResponse>> patientRegister(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PatientRegisterRequest request) {

        // service 호출
        PatientRegisterResponse patientRegister = doctorService.patientRegister(userDetails.getIdentifier(), request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "환자 등록 성공", patientRegister));
    }

    // 환자 목록 전체 조회 API
    @Operation(summary = "환자 목록 전체 조회 API", description = "의사가 본인이 담당하는 환자 전체 조회하는 API")
    @GetMapping("/doctors/patients")
    public ResponseEntity<BaseResponse<List<PatientAllSearchResponse>>> patientAll(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        List<PatientAllSearchResponse> patientAll = doctorService.patientAll(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "환자 전체 목록 조회 성공", patientAll));
    }

    // 담당 환자 특정 프로필 조회 API
    @Operation(summary = "특정 환자 조회 API", description = "환자 고유번호로 담당 환자 프로필을 조회하는 API")
    @GetMapping("/doctors/patients/{patientId}")
    public ResponseEntity<BaseResponse<PatientProfileResponse>> patientProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("patientId") Long patientId) {

        // service 호출
        PatientProfileResponse patientProfileResponse = doctorService.patientProfile(userDetails.getIdentifier(), patientId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "담당 특정 환자 정보 조회 성공", patientProfileResponse));
    }

    // 환자 이름으로 조회
    @Operation(summary = "환자 이름으로 조회 API", description = "담당 환자 이름으로 검색하는 API")
    @GetMapping("/doctors/patients/name")
    public ResponseEntity<BaseResponse<List<PatientProfileResponse>>> patientNameSearch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String name) {

        // service 호출
        List<PatientProfileResponse> response = doctorService.patientNameSearch(userDetails.getIdentifier(), name);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "이름으로 환자 조회 성공", response));
    }
}

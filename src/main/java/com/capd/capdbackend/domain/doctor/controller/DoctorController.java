package com.capd.capdbackend.domain.doctor.controller;

import com.capd.capdbackend.domain.doctor.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.doctor.dto.response.DoctorInfoResponse;
import com.capd.capdbackend.domain.doctor.dto.response.DoctorSignUpResponse;
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
    @Operation(summary = "의사 정보 조회", description = "의사가 본인의 정보 조회하는 API")
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
}

package com.capd.capdbackend.domain.user.controller;

import com.capd.capdbackend.domain.user.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.user.dto.response.SignUpResponse;
import com.capd.capdbackend.domain.user.service.DoctorService;
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

    // 회원가입 api
    @Operation(summary = "회원가입", description = "의사 회원가입 API")
    @PostMapping("/doctors")
    public ResponseEntity<BaseResponse<SignUpResponse>> signUp(
            @RequestBody @Valid DoctorSignUpRequest request) {

        // 서비스 호출
        SignUpResponse signUpResponse = doctorService.signUp(request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "회원가입을 성공했습니다.", signUpResponse));
    }

    // 의사 삭제 api
    @Operation(summary = "회원탈퇴", description = "의사인 사용자를 삭제하는 API")
    @DeleteMapping("/doctors")
    public ResponseEntity<BaseResponse<Void>> deleteDoctor(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 서비스 호출
        doctorService.deleteDoctor(userDetails.getUser().getLicenseId());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "의사인 사용자 삭제 성공", null));
    }
}

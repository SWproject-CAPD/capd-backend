package com.capd.capdbackend.domain.doctor.controller;

import com.capd.capdbackend.domain.doctor.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.doctor.dto.response.DoctorSignUpResponse;
import com.capd.capdbackend.domain.doctor.service.DoctorService;
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
}

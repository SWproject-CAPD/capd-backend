package com.capd.capdbackend.domain.auth.controller;

import com.capd.capdbackend.domain.auth.dto.request.DoctorLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.DoctorLoginResponse;
import com.capd.capdbackend.domain.auth.service.DoctorAuthService;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import com.capd.capdbackend.global.jwt.JwtProvider;
import com.capd.capdbackend.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auths")
@Tag(name = "Doctor Auth", description = "Doctor Auth 관련 API")
public class DoctorAuthController {

    private final DoctorAuthService doctorAuthService;
    private final DoctorRepository doctorRepository;
    private final JwtProvider jwtProvider;

    @Operation(summary = "의사 로그인", description = "의사가 면허번호와 비밀번호로 로그인 하는 API")
    @PostMapping("/doctors")
    public ResponseEntity<BaseResponse<DoctorLoginResponse>> doctorLogin(
            @RequestBody @Valid DoctorLoginRequest request, HttpServletResponse response) {

        // service 로직 실행
        DoctorLoginResponse doctorLoginResponse = doctorAuthService.doctorLogin(request);

        // refreshToken 가져오기
        String refreshToken = doctorRepository
                .findByLicenseId(request.getLicenseId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
                .getUser()
                .getRefreshToken();

        // Set-Cookie 설정
        jwtProvider.addJwtToCookie(response, refreshToken, "refreshToken", 60 * 60 * 24 * 7);

        // 반환
        return ResponseEntity.ok(BaseResponse.success(200, "로그인에 성공했습니다.", doctorLoginResponse));

    }
}

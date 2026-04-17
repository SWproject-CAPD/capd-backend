package com.capd.capdbackend.domain.auth.controller;

import com.capd.capdbackend.domain.auth.dto.request.PatientLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.PatientLoginResponse;
import com.capd.capdbackend.domain.auth.service.PatientAuthService;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.domain.user.repository.UserRepository;
import com.capd.capdbackend.global.exception.CustomException;
import com.capd.capdbackend.global.jwt.JwtProvider;
import com.capd.capdbackend.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auths")
@Tag(name = "Patient Auth", description = "Patient Auth 관련 API")
public class PatientAuthController {

    private final PatientAuthService patientAuthService;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Operation(summary = "환자 로그인", description = "환자가 이메일과 비밀번호로 로그인 하는 API")
    @PostMapping("/patients/tokens")
    public ResponseEntity<BaseResponse<PatientLoginResponse>> patientLogin(
            @RequestBody @Valid PatientLoginRequest request, HttpServletResponse response) {

        // service 로직 실행
        PatientLoginResponse patientLoginResponse = patientAuthService.patientLogin(request);

        // refreshToken 가져오기
        String refreshToken = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
                .getRefreshToken();

        // Set-Cookie 설정
        jwtProvider.addJwtToCookie(response, refreshToken, "refreshToken", 60 * 60 * 24 * 7);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "로그인에 성공했습니다.", patientLoginResponse));
    }

    @Operation(summary = "환자 로그아웃", description = "患者 로그아웃 api")
    @DeleteMapping("/patients/tokens")
    public ResponseEntity<BaseResponse<PatientLoginResponse>> logout(
            @RequestHeader("Authorization") String token, HttpServletResponse response) {

        // service 로직 실행 (DB에서 토큰 삭제)
        patientAuthService.patientLogout(token);

        // 브라우저에서 쿠키 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(BaseResponse.success(200, "로그아웃에 성공했습니다.", null));
    }

}

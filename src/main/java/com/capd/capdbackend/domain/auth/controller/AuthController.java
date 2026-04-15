package com.capd.capdbackend.domain.auth.controller;

import com.capd.capdbackend.domain.auth.dto.request.DoctorLoginRequest;
import com.capd.capdbackend.domain.auth.dto.request.PatientLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.LoginResponse;
import com.capd.capdbackend.domain.auth.service.AuthService;
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
@Tag(name = "Auth", description = "Auth 관련 API")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // 의사
    // 의사 로그인
    @Operation(summary = "의사 로그인", description = "의사가 로그인할때 면허번호와 비밀번호로 요청하는 API")
    @PostMapping("/doctors")
    public ResponseEntity<BaseResponse<LoginResponse>> doctorLogin(
            @RequestBody @Valid DoctorLoginRequest request, HttpServletResponse response) {

        // service 로직 실행
        LoginResponse loginResponse = authService.doctorLogin(request);

        // refreshToken 가져오기
        String refreshToken = userRepository
                .findByLicenseId(request.getLicenseId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
                .getRefreshToken();

        // Set-Cookie 설정
        jwtProvider.addJwtToCookie(response, refreshToken, "refreshToken", 1000L * 60 * 60 * 24 * 7);

        // 반환
        return ResponseEntity.ok(BaseResponse.success(200, "로그인에 성공했습니다.", loginResponse));
    }

    // 의사 로그아웃
    @Operation(summary = "의사 로그아웃", description = "로그아웃 api")
    @PostMapping("/doctors/logout")
    public ResponseEntity<String> doctorLogout(
            @RequestHeader("Authorization") String token, HttpServletResponse response) {

        authService.doctorLogout(token);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 환자
    // 환자 로그인
    @Operation(summary = "환자 로그인", description = "환자가 로그인할때 이메일과 비밀번호로 요청하는 API")
    @PostMapping("/patients")
    public ResponseEntity<BaseResponse<LoginResponse>> patientLogin(
            @RequestBody @Valid PatientLoginRequest request, HttpServletResponse response) {

        // service 로직 실행
        LoginResponse loginResponse = authService.patientLogin(request);

        // refreshToken 가져오기
        String refreshToken = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
                .getRefreshToken();

        // Set-Cookie 설정
        jwtProvider.addJwtToCookie(response, refreshToken, "refreshToken", 1000L * 60 * 60 * 24 * 7);

        // 반환
        return ResponseEntity.ok(BaseResponse.success(200, "로그인에 성공했습니다.", loginResponse));
    }

    // 환자 로그아웃
    @Operation(summary = "환자 로그아웃", description = "로그아웃 api")
    @PostMapping("/patients/logout")
    public ResponseEntity<String> patientLogout(
            @RequestHeader("Authorization") String token, HttpServletResponse response) {

        authService.patientLogout(token);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}

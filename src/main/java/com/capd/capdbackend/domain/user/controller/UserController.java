package com.capd.capdbackend.domain.user.controller;

import com.capd.capdbackend.domain.user.dto.request.EmailVerificationRequest;
import com.capd.capdbackend.domain.user.dto.request.EmailVerifyRequest;
import com.capd.capdbackend.domain.user.dto.request.PasswordChangeRequest;
import com.capd.capdbackend.domain.user.dto.request.PasswordResetRequest;
import com.capd.capdbackend.domain.user.service.EmailSendService;
import com.capd.capdbackend.domain.user.service.EmailVerificationService;
import com.capd.capdbackend.domain.user.service.UserService;
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
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final EmailSendService emailSendService;
    private final EmailVerificationService emailVerificationService;

    // 임시 비밀번호 발급 API
    @Operation(summary = "임시 비밀번호 발급", description = "사용자가 비밀번호를 까먹었을 때 이메일로 임시 비밀번호를 전송하는 API")
    @PostMapping("/users/password/reset")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @RequestBody @Valid PasswordResetRequest request) {

        // service 호출
        userService.resetPassword(request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "임시 비밀번호가 이메일로 전송되었습니다.", null));
    }

    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경하는 API")
    @PutMapping("/users/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PasswordChangeRequest request) {

        // service 호출
        userService.changePassword(userDetails.getIdentifier(), request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "비밀번호가 변경되었습니다.", null));
    }

    // 이메일 인증 코드 발송
    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입할때 이메일로 인증 코드를 발송하는 API")
    @PostMapping("/users/email/verification")
    public ResponseEntity<BaseResponse<Void>> sendVerificationCode(
            @RequestBody @Valid EmailVerificationRequest request) {

        // service 호출
        emailVerificationService.sendVerificationCode(request.getEmail());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "인증 코드가 이메일로 발송됐습니다.", null));
    }

    // 이메일 인증 코드 확인
    @Operation(summary = "이메일 인증 코드 확인", description = "회원가입할때 이메일로 발송된 인증 코드를 확인하는 API")
    @PostMapping("/users/email/verify")
    public ResponseEntity<BaseResponse<Void>> verifyCode(
            @RequestBody @Valid EmailVerifyRequest request) {

        // service 호출
        emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "이메일 인증이 완료됐습니다.", null));
    }
}

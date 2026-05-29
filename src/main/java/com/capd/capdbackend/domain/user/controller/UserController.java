package com.capd.capdbackend.domain.user.controller;

import com.capd.capdbackend.domain.user.dto.request.PasswordChangeRequest;
import com.capd.capdbackend.domain.user.dto.request.PasswordResetRequest;
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
}

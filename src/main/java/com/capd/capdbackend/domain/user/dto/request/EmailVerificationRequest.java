package com.capd.capdbackend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "이메일 인증 코드 발송 request dto", description = "사용자가 이메일을 보내서 인증코드를 받기 위해 서버에게 보내는 데이터")
public class EmailVerificationRequest {

    @Schema(description = "이메일", example = "test@naver.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;
}

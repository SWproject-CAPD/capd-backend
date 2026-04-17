package com.capd.capdbackend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "환자 로그인 request dto", description = "환자가 로그인할때 필요한 데이터")
public class PatientLoginRequest {

    @Schema(description = "환자 이메일", example = "bjh01@naver.com")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식에 맞게 입력해주세요.")
    private String email;


    @Schema(description = "사용자 비밀번호", example = "c*123456789")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
}

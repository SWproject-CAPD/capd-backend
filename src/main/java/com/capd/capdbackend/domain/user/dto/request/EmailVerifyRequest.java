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
@Schema(title = "이메일 인증 코드 확인 request dto", description = "이메일 인증 코드를 받은 후 인증코드가 맞는지 확인할때 서버에게 보내는 데이터" +
        "인증코드가 맞는지 확인하기 위해선 key(여기선 이메일)이 필요하기 때문에 email 필드 삽입")
public class EmailVerifyRequest {

    // 인증 코드를 받기 위해서는 key(여기선 이메일)가 필요하기 때문에 email 필드 넣음
    @Schema(description = "이메일", example = "test@naver.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "인증 코드", example = "123456")
    @NotBlank(message = "인증 코드는 필수입니다.")
    private String code;
}

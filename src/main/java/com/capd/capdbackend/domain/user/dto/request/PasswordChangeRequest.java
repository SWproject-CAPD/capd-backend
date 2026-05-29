package com.capd.capdbackend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "비밀번호 변경 request dto", description = "사용자가 비밀번호를 변경할때 서버한테 보내는 데이터")
public class PasswordChangeRequest {

    @Schema(description = "현재 비밀번호", example = "currentPassword123")
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "c*123456789")
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}", message = "비밀번호는 영문 대,소문자, 숫자, 특수기호를 포함한 8~20자리여야 합니다.")
    private String newPassword;
}

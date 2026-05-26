package com.capd.capdbackend.domain.doctor.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Schema(title = "의사 회원가입 요청 dto", description = "의사가 회원가입할때 서버에 요청 보내는 데이터")
@Setter
public class DoctorSignUpRequest {

    @Schema(description = "의사 면허번호", example = "123456")
    @NotBlank(message = "면허번호는 필수 입력값입니다.")
    private String licenseId;

    @Schema(description = "의사 이름", example = "김정모")
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    @Schema(description = "이메일", example = "kjm02@naver.com")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Schema(description = "비밀번호", example = "c*123456789")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}", message = "비밀번호는 영문 대,소문자, 숫자, 특수기호를 포함한 8~20자리여야 합니다.")
    private String password;

    @Schema(description = "전화번호", example = "010-1111-1111")
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식(000-0000-0000)에 맞게 입력해주세요.")
    private String phone;
}

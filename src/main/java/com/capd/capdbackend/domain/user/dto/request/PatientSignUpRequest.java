package com.capd.capdbackend.domain.user.dto.request;

import com.capd.capdbackend.domain.user.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(title = "회원가입 요청 dto", description = "환자 회원가입할때 필요한 데이터")
public class PatientSignUpRequest {

    @Schema(description = "이메일", example = "kjm02@naver.com")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Schema(description = "비밀번호", example = "c*123456789")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}", message = "비밀번호는 영문 대,소문자, 숫자, 특수기호를 포함한 8~20자리여야 합니다.")
    private String password;

    @Schema(description = "사용자 이름", example = "배재훈")
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    @Schema(description = "생년월일", example = "2001-10-28")
    @NotNull(message = "생년월일은 필수 입력값입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;

    @Schema(description = "전화번호", example = "010-2222-2222")
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    private String phone;

    @Schema(description = "성별", example = "MAN")
    @NotNull(message = "성별은 필수 입력값입니다.")
    private Gender gender;
}

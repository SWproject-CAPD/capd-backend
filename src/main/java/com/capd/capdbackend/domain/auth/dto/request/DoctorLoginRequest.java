package com.capd.capdbackend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "의사 로그인 request dto", description = "의사가 로그인할때 필요한 데이터")
public class DoctorLoginRequest {

    @Schema(description = "의사 면허번호", example = "123456")
    @NotBlank(message = "면허번호는 필수 입력값입니다.")
    private String licenseId;


    @Schema(description = "의사 비밀번호", example = "c*123456789")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
}

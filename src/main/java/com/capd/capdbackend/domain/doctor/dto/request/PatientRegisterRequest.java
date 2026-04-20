package com.capd.capdbackend.domain.doctor.dto.request;

import com.capd.capdbackend.domain.patient.entity.Sex;
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
@Schema(title = "환자 등록 요청 dto", description = "의사가 가입되 환자의 전화번호로 본인의 환자로 등록하는 데이터")
@Setter
public class PatientRegisterRequest {

    @Schema(description = "전화번호", example = "010-2222-2222")
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식(000-0000-0000)에 맞게 입력해주세요.")
    private String phone;
}

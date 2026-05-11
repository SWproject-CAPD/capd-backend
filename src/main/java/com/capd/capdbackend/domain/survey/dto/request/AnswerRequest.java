package com.capd.capdbackend.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "설문 답변 request dto", description = "환자가 질문에 답변할때 서버에서 반환하는 데이터")
public class AnswerRequest {

    @Schema(description = "답변 내용", example = "국물류")
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String answer;
}

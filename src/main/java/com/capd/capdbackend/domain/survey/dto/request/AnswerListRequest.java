package com.capd.capdbackend.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "설문 답변 전체 제출 request dto", description = "환자가 질문 전체를 제출할때 서버에서 반환하는 데이터")
public class AnswerListRequest {

    @Schema(description = "답변 목록")
    @NotNull(message = "답변 목록은 필수입니다.")
    @Valid
    private List<AnswerRequest> answers;
}

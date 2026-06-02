package com.capd.capdbackend.domain.survey.dto.request;

import com.capd.capdbackend.domain.survey.entity.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "의사 질문 수동 생성 request dto", description = "의사가 설문을 수동으로 만들어 환자에게 전달할때 서버에게 보내는 데이터")
public class PassiveQuestionRequest {

    @Schema(description = "질문 내용", example = "최근 투석 후 불편한 점이 있었나요?")
    @NotBlank(message = "질문 내용은 필수입니다.")
    private String question;

    @Schema(description = "질문 유형", example = "YES_NO")
    @NotNull(message = "질문 유형은 필수입니다.")
    private QuestionType type;

    @Schema(description = "객관식 보기 (객관식이 아니라면 생략이 가능함)", example = "[\"예\", \"아니요\"]")
    private String options;
}

package com.capd.capdbackend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "설문 답변 응답 dto", description = "환자가 작성한 질문에 대한 답변을 조회할때 서버에서 반환하는 데이터")
public class AnswerResponse {

    @Schema(description = "답변 고유번호", example = "1")
    private Long answerId;

    @Schema(description = "질문 고유번호", example = "1")
    private Long questionId;

    @Schema(description = "환자 ID", example = "5")
    private Long patientId;

    @Schema(description = "환자 이름", example = "홍길동")
    private String patientName;

    @Schema(description = "질문 내용", example = "어제 저녁 식사로 주로 어떤 음식을 드셨나요?")
    private String question;

    @Schema(description = "답변 내용", example = "국물류")
    private String answer;

    @Schema(description = "AI 설명")
    private String aiExplain;

    @Schema(description = "답변 일시")
    private LocalDateTime createdAt;
}

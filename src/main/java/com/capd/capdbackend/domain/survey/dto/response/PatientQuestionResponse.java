package com.capd.capdbackend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "환자용 질문 응답 dto", description = "환자가 질문을 받아볼때 서버에서 반환하는 데이터")
public class PatientQuestionResponse {

    @Schema(description = "질문 고유번호", example = "1")
    private Long questionId;

    @Schema(description = "예약 날짜", example = "2026-05-10T14:00:00")
    private LocalDateTime reservationDate;

    @Schema(description = "질문 내용", example = "어제 저녁 식사로 주로 어떤 음식을 드셨나요?")
    private String question;

    @Schema(description = "질문 유형", example = "MULTIPLE_CHOICE")
    private String type;

    @Schema(description = "객관식 보기", example = "[\"국물류\", \"튀김류\", \"육류\"]")
    private String options;

    @Schema(description = "답변 여부", example = "false")
    private boolean answered;

    @Schema(description = "답변 내용", example = "국물류")
    private String answer;
}

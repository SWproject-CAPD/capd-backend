package com.capd.capdbackend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "의사용 질문 응답 dto", description = "AI가 환자별 질문을 생성하고 의사가 승인하기 전 서버에서 반환하는 데이터")
public class QuestionResponse {

    @Schema(description = "질문 고유번호", example = "1")
    private Long questionId;

    @Schema(description = "예약 고유번호", example = "1")
    private Long reservationId;

    @Schema(description = "예약 날짜", example = "2026-05-10T14:00:00")
    private LocalDateTime reservationDate;

    @Schema(description = "환자 ID", example = "5")
    private Long patientId;

    @Schema(description = "환자 이름", example = "홍길동")
    private String patientName;

    @Schema(description = "질문 내용", example = "어제 저녁 식사로 주로 어떤 음식을 드셨나요?")
    private String question;

    @Schema(description = "질문 유형", example = "MULTIPLE_CHOICE")
    private String type;

    @Schema(description = "객관식 보기", example = "[\"국물류\", \"튀김류\", \"육류\"]")
    private String options;

    @Schema(description = "AI 추천 근거", example = "최근 3주간 체중 증가 추세로 식단 관련 질문 추천")
    private String questionReason;

    @Schema(description = "승인 상태", example = "PENDING")
    private String status;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
}

package com.capd.capdbackend.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "챗봇 응답 dto", description = "사용자가 AI 챗봇에게 질문을 하고 서버로부터 응답 받는 데이터")
public class ChatResponse {

    @Schema(description = "채팅 고유번호", example = "1")
    private Long chatId;

    @Schema(description = "메시지 순서", example = "1")
    private int displayOrder;

    @Schema(description = "사용자 질문")
    private String userText;

    @Schema(description = "AI 응답")
    private String aiText;

    @Schema(description = "생성 일시", example = "2026-05-18T13:40:00")
    private LocalDateTime createdAt;
}

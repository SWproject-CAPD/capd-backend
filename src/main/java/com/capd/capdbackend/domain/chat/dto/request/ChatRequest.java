package com.capd.capdbackend.domain.chat.dto.request;

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
@Schema(title = "챗봇 질문 request dto", description = "사용자가 AI 챗봇에게 질문할때 서버에게 보내는 데이터")
public class ChatRequest {

    @Schema(description = "사용자 질문", example = "제 혈압이 정상 범주에 있나요 ?")
    @NotBlank(message = "질문은 필수입니다.")
    private String userText;
}

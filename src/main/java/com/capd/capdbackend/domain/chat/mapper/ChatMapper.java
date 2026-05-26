package com.capd.capdbackend.domain.chat.mapper;

import com.capd.capdbackend.domain.chat.dto.response.ChatResponse;
import com.capd.capdbackend.domain.chat.entity.ChatLogEntity;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    // entity -> dto
    public ChatResponse toResponse(ChatLogEntity entity) {
        return ChatResponse.builder()
                .chatId(entity.getChatId())
                .displayOrder(entity.getDisplayOrder())
                .userText(entity.getUserText())
                .aiText(entity.getAiText())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

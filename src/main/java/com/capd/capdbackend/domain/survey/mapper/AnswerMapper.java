package com.capd.capdbackend.domain.survey.mapper;

import com.capd.capdbackend.domain.survey.dto.response.AnswerResponse;
import com.capd.capdbackend.domain.survey.entity.AnswerResultEntity;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapper {

    // entity -> response
    public AnswerResponse toResponse(AnswerResultEntity entity) {
        return AnswerResponse.builder()
                .answerId(entity.getAnswerId())
                .questionId(entity.getQuestion().getQuestionId())
                .patientId(entity.getPatient().getPatientId())
                .patientName(entity.getPatient().getUser().getUserName())
                .question(entity.getQuestion().getQuestion())
                .answer(entity.getAnswer())
                .aiExplain(entity.getAiExplain())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

package com.capd.capdbackend.domain.survey.mapper;

import com.capd.capdbackend.domain.survey.dto.response.PatientQuestionResponse;
import com.capd.capdbackend.domain.survey.dto.response.QuestionResponse;
import com.capd.capdbackend.domain.survey.entity.QuestionRecommendEntity;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    // entity -> 의사용 response
    public QuestionResponse toQuestionResponse(QuestionRecommendEntity entity) {
        return QuestionResponse.builder()
                .questionId(entity.getQuestionId())
                .reservationId(entity.getReservation().getReservationId())
                .reservationDate(entity.getReservation().getReservationDate())
                .patientId(entity.getPatient().getPatientId())
                .patientName(entity.getPatient().getUser().getUserName())
                .question(entity.getQuestion())
                .type(entity.getType().name())
                .options(entity.getOptions())
                .questionReason(entity.getQuestionReason())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // entity -> 환자용 response
    public PatientQuestionResponse toPatientQuestionResponse(QuestionRecommendEntity entity, boolean answered, String answer) {
        return PatientQuestionResponse.builder()
                .questionId(entity.getQuestionId())
                .reservationDate(entity.getReservation().getReservationDate())
                .question(entity.getQuestion())
                .type(entity.getType().name())
                .options(entity.getOptions())
                .answered(answered)
                .answer(answer)
                .build();
    }
}

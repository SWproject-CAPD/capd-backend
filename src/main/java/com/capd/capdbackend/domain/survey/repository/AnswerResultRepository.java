package com.capd.capdbackend.domain.survey.repository;

import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.survey.entity.AnswerResultEntity;
import com.capd.capdbackend.domain.survey.entity.QuestionRecommendEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerResultRepository extends JpaRepository<AnswerResultEntity, Long> {

    // 의사가 특정 환자의 전체 답변 조회
    List<AnswerResultEntity> findAllByPatientOrderByCreatedAtDesc(PatientEntity patient);

    // 특정 질문에 대한 답변 조회
    Optional<AnswerResultEntity> findByQuestionAndPatient(QuestionRecommendEntity question, PatientEntity patient);

    // 환자가 이미 답변했는지 확인
    boolean existsByQuestionAndPatient(QuestionRecommendEntity question, PatientEntity patient);
}

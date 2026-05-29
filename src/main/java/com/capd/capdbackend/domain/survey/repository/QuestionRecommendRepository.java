package com.capd.capdbackend.domain.survey.repository;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import com.capd.capdbackend.domain.survey.entity.QuestionRecommendEntity;
import com.capd.capdbackend.domain.survey.entity.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRecommendRepository extends JpaRepository<QuestionRecommendEntity, Long> {

    // 의사가 특정 예약의 질문 목록 조회
    List<QuestionRecommendEntity> findAllByReservationOrderByCreatedAtDesc(ReservationEntity reservation);

    // 환자가 예약된 날짜를 기준으로 승인된 질문 조회
    List<QuestionRecommendEntity> findAllByReservationAndStatusOrderByCreatedAtDesc(ReservationEntity reservation, QuestionStatus status);

    // 질문 ID로 조회
    Optional<QuestionRecommendEntity> findByQuestionId(Long questionId);

    // 예약에 연관된 질문 목록 조회
    List<QuestionRecommendEntity> findAllByReservation(ReservationEntity reservation);

    // 예약에 연관된 질문 전체 삭제
    void deleteAllByReservation(ReservationEntity reservation);
}

package com.capd.capdbackend.domain.survey.entity;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "question_recommends")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class QuestionRecommendEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @Column(nullable = false, length = 1000)
    private String question; // 질문 내용

    @Column(length = 1000)
    private String questionReason; // AI 추천 근거

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status = QuestionStatus.PENDING; // 승인 상태 (처음은 대기 상태)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type; // 질문 유형

    @Column(length = 1000)
    private String options; // 객관식 보기

    // 의사가 질문 승인
    public void approve() {
        this.status = QuestionStatus.APPROVED;;
    }

    // 의사가 질문 거절
    public void reject() {
        this.status = QuestionStatus.REJECTED;
    }

    // 되돌리기
    public void reset() {
        this.status = QuestionStatus.PENDING;
    }
}

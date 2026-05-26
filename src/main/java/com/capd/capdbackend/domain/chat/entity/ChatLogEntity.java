package com.capd.capdbackend.domain.chat.entity;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "chat_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    @Column(nullable = false)
    private int displayOrder; // 메시지 표시 순서

    @Column(nullable = false, length = 1000)
    private String userText; // 사용자 입력 메시지

    @Column(nullable = false, length = 3000)
    private String aiText; // AI 응답 메시지
}

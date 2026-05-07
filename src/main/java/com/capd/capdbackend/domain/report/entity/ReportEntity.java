package com.capd.capdbackend.domain.report.entity;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReportEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId; // 주간 보고서 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(nullable = false)
    private LocalDate startDate; // 보고서 기간 시작일

    @Column(nullable = false)
    private LocalDate endDate; // 보고서 기간 종료일

    @Column(length = 500)
    private String weightSummary; // 체중 변화 요약

    @Column(length = 500)
    private String bpSummary; // 혈압 변화 요약

    @Column(length = 500)
    private String bloodSugarSummary; // 혈당 변화 요약

    @Column(length = 500)
    private String ufSummary; // 총초여과량 변화 요약

    @Column(length = 500)
    private String anomalySummary; // 이상치 발생 요약

    @Column(length = 2000)
    private String docSummary; // 보고서 AI 요약
}

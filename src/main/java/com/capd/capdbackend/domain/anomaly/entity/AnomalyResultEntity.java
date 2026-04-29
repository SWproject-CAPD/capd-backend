package com.capd.capdbackend.domain.anomaly.entity;

import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "anomaly_results")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AnomalyResultEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long anomalyId; // 이상치 탐지 결과 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient; // 어떤 환자의 이상치 결과인지

    @Column(nullable = false)
    private LocalDate analysisDate; // 분석 대상 날짜

    @Column(nullable = false)
    private int riskLevel; // 정상(1), 주의(2), 위험(3)

    @Column(nullable = false)
    private float anomalyScore; // 이상치 결과 점수 (높을 수록 정상)

    @Column(nullable = false)
    private String statusMessage; // 정상, 주의, 위험 문자 형태

    @Column(length = 1000)
    private String topCauses; // 이상치 원인 상위 3개

    // 같은 날짜의 투석일지를 재분석할때 사용하는 메서드
    public void updateResult(int riskLevel, float anomalyScore,
                             String statusMessage, String topCauses) {
        this.riskLevel = riskLevel;
        this.anomalyScore = anomalyScore;
        this.statusMessage = statusMessage;
        this.topCauses = topCauses;
    }
}

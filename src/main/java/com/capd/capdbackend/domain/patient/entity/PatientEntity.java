package com.capd.capdbackend.domain.patient.entity;

import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "patients")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PatientEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId; // 환자 고유번호

    // UserEntity에서 이름, 이메일, 비밀번호, 전화번호, 권한을 받을 수 있음 (user.get()...)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // UserEntity의 userId와 1대1 매핑

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sex sex; // 성별

    @Column(nullable = false)
    private int age; // 환자 나이

    private Float heightCm; // 환자 키

    private Float baselineWeightKg; // 투석을 처음 시작할때 측정한 기준 체중 (kg)

    private Float baselineBmi; // 투석 시작 시점 BMI

    @Column(length = 1000)
    private String primaryPattern; // 의사가 처방한 기본 CAPD 교환 방식

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> comorbidities; // 투석 외에 보유한 질환 목록

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> writingPersona; // 환자에게 AI가 답변할 때 사용할 말투, 설명 수준

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> adherencePersona; // 환자가 의사 지시를 얼마나 잘 따르는지에 대한 경향 정보
}

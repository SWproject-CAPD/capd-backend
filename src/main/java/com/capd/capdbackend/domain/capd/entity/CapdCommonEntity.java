package com.capd.capdbackend.domain.capd.entity;

import com.capd.capdbackend.domain.capd.dto.request.CapdCommonUpdateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdCreateRequest;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "capd_commons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CapdCommonEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long capdId; // 투석일지 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(nullable = false)
    private boolean cloudyDialysate; // 배액이 뿌옇게 나왔는지 여부 (의사가 확인)

    @Column(nullable = false)
    private int urinationCount; // 하루동안 소변을 본 횟수

    @Column(nullable = false)
    private float totalUltrafiltration; // 그날 전체 교환 세션의 초여과량 합계 (g)

    @Column(nullable = false)
    private float bodyWeight; // 당일 측정 체중 (kg)

    @Column(nullable = false)
    private int bloodPressureSys; // 혈압 측정 시 높은 수치 (mmHg)

    @Column(nullable = false)
    private int bloodPressureDia; // 혈압 측정 시 낮은 수치 (mmHg)

    @Column(nullable = false)
    private float fastingBloodSugar; // 공복 혈당 (mg/dL)

    @Column(length = 1000)
    private String note; // 환자가 자유롭게 입력하는 당일 특이사항

    @Column(nullable = false)
    private LocalDate date; // 해당 일지 날짜

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CapdStatus status = CapdStatus.TEMP; // 기본값 임시저장

    @Builder.Default
    @OneToMany(mappedBy = "capdCommon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CapdSessionEntity> sessions = new ArrayList<>();

    // 총초여과량 (총 제수량 합계) 메서드
    public void calculateTotalUltrafiltration() {
        this.totalUltrafiltration = (float) this.sessions.stream()
                .mapToDouble(CapdSessionEntity::getUltrafiltration)
                .sum();
    }

    // 공통 투석일지 정보 업데이트 메서드
    public void updateCommonInfo(CapdCreateRequest request) {
        this.cloudyDialysate = request.isCloudyDialysate();
        this.urinationCount = request.getUrinationCount();
        this.bodyWeight = request.getBodyWeight();
        this.bloodPressureSys = request.getBloodPressureSys();
        this.bloodPressureDia = request.getBloodPressureDia();
        this.fastingBloodSugar = request.getFastingBloodSugar();
        this.note = request.getNote();
    }

    // 공통 투석일지 수정 업데이트 메서드
    public void updateCommonInfoFromRequest(CapdCommonUpdateRequest request) {
        this.cloudyDialysate = request.isCloudyDialysate();
        this.urinationCount = request.getUrinationCount();
        this.bodyWeight = request.getBodyWeight();
        this.bloodPressureSys = request.getBloodPressureSys();
        this.bloodPressureDia = request.getBloodPressureDia();
        this.fastingBloodSugar = request.getFastingBloodSugar();
        this.note = request.getNote();
    }
}

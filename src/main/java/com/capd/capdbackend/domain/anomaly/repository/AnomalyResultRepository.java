package com.capd.capdbackend.domain.anomaly.repository;

import com.capd.capdbackend.domain.anomaly.entity.AnomalyResultEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnomalyResultRepository extends JpaRepository<AnomalyResultEntity, Long> {

    // 특정 환자 전체 이상치 결과 최신순 조회
    List<AnomalyResultEntity> findAllByPatientOrderByAnalysisDateDesc(
            PatientEntity patient);

    // 특정 날짜 결과 조회 (재분석 시 덮어쓰기용)
    Optional<AnomalyResultEntity> findByPatientAndAnalysisDate(
            PatientEntity patient, LocalDate analysisDate);
}

package com.capd.capdbackend.domain.capd.repository;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdStatus;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CapdCommonRepository extends JpaRepository<CapdCommonEntity, Long> {

    // 환자가 특정 날짜의 투석 일지를 조회하거나 수정하고 싶을때
    Optional<CapdCommonEntity> findByPatientAndDate(PatientEntity patient, LocalDate date);

    // 의사나 환자가 투석 일지를 최신순으로 보고 싶을때
    List<CapdCommonEntity> findAllByPatientOrderByDateDesc(PatientEntity patient);

    // 날짜 + status로 조회
    Optional<CapdCommonEntity> findByPatientAndDateAndStatus(PatientEntity patient, LocalDate date, CapdStatus status);

    // 투석일지 id로 조회
    Optional<CapdCommonEntity> findByCapdId(Long capdId);

    // 의사가 제출한 투석일지를 최신순으로 조회
    List<CapdCommonEntity> findAllByPatientAndStatusOrderByDateDesc(PatientEntity patient, CapdStatus status);

    // 날짜 기준 이전 7일치 SUBMITTED 기록 조회
    List<CapdCommonEntity> findTop7ByPatientAndStatusAndDateLessThanEqualOrderByDateDesc(
            PatientEntity patient, CapdStatus status, LocalDate date);
}

package com.capd.capdbackend.domain.capd.repository;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CapdCommonRepository extends JpaRepository<CapdCommonEntity, Long> {

    // 환자가 특정 날짜의 투석 일지를 조회하거나 수정하고 싶을때
    Optional<CapdCommonEntity> findByPatientAndDate(PatientEntity patient, LocalDate date);

    // 의사나 환자가 투석 일지를 최신순으로 보고 싶을때
    Optional<CapdCommonEntity> findAllByPatientOrderByDateDesc(PatientEntity patient);


}

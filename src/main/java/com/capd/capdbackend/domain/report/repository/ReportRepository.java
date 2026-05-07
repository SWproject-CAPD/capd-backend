package com.capd.capdbackend.domain.report.repository;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.report.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity ,Long> {

    // 의사 + 환자 기준 전체 보고서 최신순 조회
    List<ReportEntity> findAllByDoctorAndPatientOrderByStartDateDesc(DoctorEntity doctor, PatientEntity patient);

    // 같은 기간 보고서 중복 확인
    Optional<ReportEntity> findByDoctorAndPatientAndStartDateAndEndDate(DoctorEntity doctor, PatientEntity patient, LocalDate startDate, LocalDate endDate);
}

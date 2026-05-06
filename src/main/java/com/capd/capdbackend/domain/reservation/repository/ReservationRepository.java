package com.capd.capdbackend.domain.reservation.repository;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // 의사의 전체 예약 목록 최신순 조회
    List<ReservationEntity> findAllByDoctorOrderByReservationDateAsc(DoctorEntity doctor);

    // 환자의 전체 예약 목록 조회
    List<ReservationEntity> findAllByPatientOrderByReservationDateAsc(PatientEntity patient);

    // 같은 날짜 중복 예약 확인
    Optional<ReservationEntity> findByDoctorAndPatientAndReservationDate(DoctorEntity doctor, PatientEntity patient, LocalDateTime reservationDate);
}

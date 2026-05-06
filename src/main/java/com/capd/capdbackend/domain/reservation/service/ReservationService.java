package com.capd.capdbackend.domain.reservation.service;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.exception.DoctorErrorCode;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.reservation.dto.request.ReservationCreateRequest;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationCreateResponse;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationDoctorReadResponse;
import com.capd.capdbackend.domain.reservation.dto.response.ReservationPatientReadResponse;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import com.capd.capdbackend.domain.reservation.exception.ReservationErrorCode;
import com.capd.capdbackend.domain.reservation.mapper.ReservationCreateMapper;
import com.capd.capdbackend.domain.reservation.mapper.ReservationDoctorReadMapper;
import com.capd.capdbackend.domain.reservation.mapper.ReservationPatientReadMapper;
import com.capd.capdbackend.domain.reservation.repository.ReservationRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReservationCreateMapper reservationCreateMapper;
    private final ReservationPatientReadMapper reservationPatientReadMapper;
    private final ReservationDoctorReadMapper reservationDoctorReadMapper;

    // 의사가 진료 예약 생성
    @Transactional
    public ReservationCreateResponse reservationCreate(String licenseId, ReservationCreateRequest request) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByPatientId(request.getPatientId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 담당 환자인지 확인
        if (patient.getDoctor() == null || !patient.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        // 중복 예약 확인
        if (reservationRepository.findByDoctorAndPatientAndReservationDate(doctor, patient, request.getReservationDate()).isPresent()) {
            throw new CustomException(ReservationErrorCode.RESERVATION_DUPLICATE);
        }

        // dto -> entity
        ReservationEntity reservation = reservationCreateMapper.toEntity(request, doctor, patient);

        // db 저장
        reservationRepository.save(reservation);

        // 로그 출력
        log.info("예약 생성 완료: doctorId={}, patientId={}, date={}", doctor.getDoctorId(), patient.getPatientId(), request.getReservationDate());

        // entity -> response dto
        return reservationCreateMapper.toResponse(reservation);
    }

    // 환자가 본인의 예약 날짜를 조회
    public List<ReservationPatientReadResponse> patientReservation(String email) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 예약 목록 조회
        List<ReservationEntity> list = reservationRepository.findAllByPatientOrderByReservationDateAsc(patient);

        // entity -> dto
        return list.stream()
                .map(reservationPatientReadMapper::toResponse)
                .toList();
    }

    // 의사가 특정 날짜 진료 예약 조회
    public List<ReservationDoctorReadResponse> doctorReservation(String licenseId, LocalDate date) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<ReservationEntity> list = reservationRepository.findAllByDoctorAndReservationDateBetweenOrderByReservationDateAsc(doctor, start, end);

        // entity -> dto
        return list.stream()
                .map(reservationDoctorReadMapper::toResponse)
                .toList();
    }
}

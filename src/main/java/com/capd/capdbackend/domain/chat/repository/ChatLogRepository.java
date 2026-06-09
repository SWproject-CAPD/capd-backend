package com.capd.capdbackend.domain.chat.repository;

import com.capd.capdbackend.domain.chat.entity.ChatLogEntity;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatLogRepository extends JpaRepository<ChatLogEntity, Long> {

    // 환자 채팅 기록 오름차순으로 조회
    List<ChatLogEntity> findAllByPatientOrderByDisplayOrderAsc(PatientEntity patient);

    // 의사 채팅 기록 오름차순으로 조회
    List<ChatLogEntity> findAllByDoctorOrderByDisplayOrderAsc(DoctorEntity doctor);

    // 환자 채팅 기록 개수 (displayOrder 계산용)
    int countByPatient(PatientEntity patient);

    // 의사 채팅 기록 개수 (displayOrder 계산용)
    int countByDoctor(DoctorEntity doctor);

    // 의사가 회원 탈퇴시
    void deleteAllByDoctor(DoctorEntity doctor);

    // 환자가 회원 탈퇴시
    void deleteAllByPatient(PatientEntity patient);
}

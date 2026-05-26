package com.capd.capdbackend.domain.patient.repository;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    // user 도메인에 있는 사용자와 patient 도메인에 있는 환자가 같은 사람인지 확인
    Optional<PatientEntity> findByUser(UserEntity user);

    // 이메일을 통해 환자 정보 가져오기
    @EntityGraph(attributePaths = {"user"})
    Optional<PatientEntity> findByUserEmail(String email);

    // 한명의 의사가 담당하는 환자 목록 전체 보기
    @EntityGraph(attributePaths = {"user"})
    List<PatientEntity> findByDoctor(DoctorEntity doctor);

    // 환자 고유번호로 환자 조회
    Optional<PatientEntity> findByPatientId(Long patientId);

    // 환자 이름으로 의사가 담당 환자 검색
    @EntityGraph(attributePaths = {"user"})
    List<PatientEntity> findByDoctorAndUser_UserNameContaining(DoctorEntity doctor, String name);
}

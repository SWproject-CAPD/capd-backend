package com.capd.capdbackend.domain.patient.repository;

import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    // user 도메인에 있는 사용자와 patient 도메인에 있는 환자가 같은 사람인지 확인
    Optional<PatientEntity> findByUser(UserEntity user);
}

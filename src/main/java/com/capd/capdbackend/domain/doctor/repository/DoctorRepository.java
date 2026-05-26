package com.capd.capdbackend.domain.doctor.repository;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {

    // 면허번호 중복 체크
    boolean existsByLicenseId(String licenseId);

    // 면허번호로 회원 정보 조회
    @EntityGraph(attributePaths = {"user"})
    Optional<DoctorEntity> findByLicenseId(String licenseId);
}

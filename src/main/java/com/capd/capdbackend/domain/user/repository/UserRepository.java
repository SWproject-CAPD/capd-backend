package com.capd.capdbackend.domain.user.repository;

import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 의사 면허번호 중복 체크
    boolean existsByLicenseId(String licenseId);

    // 전화번호 중복 체크
    boolean existsByPhone(String phone);

    // 이메일로 환자 정보 조회
    Optional<UserEntity> findByEmail(String email);

    // 의사 면허번호로 의사 정보 조회
    Optional<UserEntity> findByLicenseId(String licenseId);
}

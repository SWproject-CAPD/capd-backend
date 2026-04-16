package com.capd.capdbackend.domain.user.repository;

import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 전화번호 중복 체크
    boolean existsByPhone(String phone);

    // 이메일로 회원 정보 조회
    Optional<UserEntity> findByEmail(String email);
}

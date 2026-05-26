package com.capd.capdbackend.domain.capd.repository;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CapdSessionRepository extends JpaRepository<CapdSessionEntity, Long> {

    // 공통 투석 일지에 몇개의 세션이 등록됐는지 확인
    int countByCapdCommon(CapdCommonEntity capdCommon);

    // 특정 공통 투석 일지 내에서 특정 세션 회수가 채워졌는지 확인
    Optional<CapdSessionEntity> findByCapdCommonAndSessionNumber(CapdCommonEntity capdCommon, int sessionNumber);

    // 해당 세션 투석일지가 이미 존재하는지 여부
    boolean existsByCapdCommonAndSessionNumber(CapdCommonEntity capdCommon, int sessionNumber);

    // 세션 투석일지 id로 조회
    Optional<CapdSessionEntity> findByCapdSessionId(Long capdSessionId);
}

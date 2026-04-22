package com.capd.capdbackend.domain.capd.service;

import com.capd.capdbackend.domain.capd.dto.request.CapdCommonCreateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdSessionCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdSessionEntity;
import com.capd.capdbackend.domain.capd.exception.CapdErrorCode;
import com.capd.capdbackend.domain.capd.mapper.CapdCommonCreateMapper;
import com.capd.capdbackend.domain.capd.mapper.CapdSessionCreateMapper;
import com.capd.capdbackend.domain.capd.repository.CapdCommonRepository;
import com.capd.capdbackend.domain.capd.repository.CapdSessionRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CapdService {

    private final CapdCommonRepository capdCommonRepository;
    private final CapdSessionRepository capdSessionRepository;
    private final PatientRepository patientRepository;
    private final CapdCommonCreateMapper capdCommonCreateMapper;
    private final CapdSessionCreateMapper capdSessionCreateMapper;

    // 세션 투석일지 제출
    @Transactional
    public CapdCommonResponse createCommonCapd(Long patientId, CapdCommonCreateRequest request) {

        // 환자 확인
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 날짜에 투석 일지가 있는지 확인
        Optional<CapdCommonEntity> existCapd = capdCommonRepository.findByPatientAndDate(patient, request.getDate());

        CapdCommonEntity common;

        // 투석일지 조회
        if (existCapd.isPresent()) {

            // 투석일지가 있으면 기존 값에 덮어쓰기
            common = existCapd.get();
            common.updateCommonInfo(request);
        }
        else {

            // 투석일지가 없으면 새로 생성해서 db에 저장
            common = capdCommonRepository.save(capdCommonCreateMapper.toCommonEntity(request, patient));
        }

        // entity -> response DTO
        return capdCommonCreateMapper.toCommonResponse(common);
    }

    // 회차별 세션 투석일지 제출
    @Transactional
    public CapdSessionResponse createSessionCapd(Long patientId, CapdSessionCreateRequest request) {

        // 환자 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 공통 부모일지 찾거나 생성
        CapdCommonEntity common = capdCommonRepository.findByPatientAndDate(patient, request.getDate())
                .orElseGet(() -> {
                    CapdCommonCreateRequest defaultRequest = CapdCommonCreateRequest.builder().date(request.getDate()).build(); // 🌟 수정
                    return capdCommonRepository.save(capdCommonCreateMapper.toCommonEntity(defaultRequest, patient));
                });

        // 해당 회차 세션 투석일지가 존재하는지 확인
        if (capdSessionRepository.existsByCapdCommonAndSessionNumber(common, request.getSessionNumber())) {
            throw new CustomException(CapdErrorCode.ALREADY_EXIST_SESSION);
        }

        // session 엔티티 생성
        CapdSessionEntity session = capdSessionCreateMapper.toSessionEntity(request, common);

        // db에 저장
        capdSessionRepository.save(session);

        // 해당 세션 투석일지를 리스트에 추가
        common.getSessions().add(session);

        // 리스트를 돌면서 총초여과량 계산
        common.calculateTotalUltrafiltration();

        // 방금 제출한 세션에 대한 응답 반환
        return capdSessionCreateMapper.toSessionResponse(session);
    }
}

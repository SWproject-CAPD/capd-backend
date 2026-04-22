package com.capd.capdbackend.domain.capd.service;

import com.capd.capdbackend.domain.capd.dto.request.CapdCommonCreateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdSessionCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdSessionEntity;
import com.capd.capdbackend.domain.capd.exception.CapdErrorCode;
import com.capd.capdbackend.domain.capd.mapper.CapdCommonMapper;
import com.capd.capdbackend.domain.capd.mapper.CapdSessionMapper;
import com.capd.capdbackend.domain.capd.repository.CapdCommonRepository;
import com.capd.capdbackend.domain.capd.repository.CapdSessionRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.exception.PatientErrorCode;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CapdService {

    private final CapdCommonRepository capdCommonRepository;
    private final CapdSessionRepository capdSessionRepository;
    private final PatientRepository patientRepository;
    private final CapdCommonMapper capdCommonMapper;
    private final CapdSessionMapper capdSessionMapper;

    // 투석일지 제출
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

            // 이미 제출한 하루 공통 투석일지가 있으면 에러 출력
            if (common.isSubmitted()) {
                throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
            }

            // 투석일지가 있으면 기존 값에 덮어쓰기 후 마감 처리
            common.updateCommonInfo(request);
            common.setSubmitted(true);
        }
        else {

            // 투석일지가 없으면 새로 생성해서 마감 처리 후 db에 저장
            common = capdCommonMapper.toCommonEntity(request, patient);
            common.setSubmitted(true); // 마감 완료!
            common = capdCommonRepository.save(common);
        }

        // entity -> response DTO
        return capdCommonMapper.toCommonResponse(common);
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
                    return capdCommonRepository.save(capdCommonMapper.toCommonEntity(defaultRequest, patient));
                });

        // 공통 투석일지가 제출 되었으면 세션 투석일지도 작성 못함
        if (common.isSubmitted()) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        // 해당 회차 세션 투석일지가 존재하는지 확인
        if (capdSessionRepository.existsByCapdCommonAndSessionNumber(common, request.getSessionNumber())) {
            throw new CustomException(CapdErrorCode.ALREADY_EXIST_SESSION);
        }

        // session 엔티티 생성
        CapdSessionEntity session = capdSessionMapper.toSessionEntity(request, common);

        // db에 저장
        capdSessionRepository.save(session);

        // 해당 세션 투석일지를 리스트에 추가
        common.getSessions().add(session);

        // 리스트를 돌면서 총초여과량 계산
        common.calculateTotalUltrafiltration();

        // 방금 제출한 세션에 대한 응답 반환
        return capdSessionMapper.toSessionResponse(session);
    }

    // 투석일지 전체 목록 조회
    public List<CapdCommonResponse> capdAllRead(Long patientId) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 환자 투석일지 최신순으로 가져오기
        List<CapdCommonEntity> list = capdCommonRepository.findAllByPatientOrderByDateDesc(patient);

        // 변환된 dto 담을 빈 상자
        List<CapdCommonResponse> resultList = new ArrayList<>();

        for (CapdCommonEntity capdCommon : list) {

            // entity -> dto 변환
            CapdCommonResponse capdCommonResponse = capdCommonMapper.toCommonResponse(capdCommon);

            // list에 추가
            resultList.add(capdCommonResponse);
        }

        // list 반환
        return resultList;
    }

    // 날짜 이용한 투석일지 단건 조회
    public CapdCommonResponse capdProfileRead(Long patientId, LocalDate date) {

        // 환자 유저 확인
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 날짜로 투석일지가 있는지 확인
        CapdCommonEntity capdCommon = capdCommonRepository.findByPatientAndDate(patient, date)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // entity -> dto 변환
        return capdCommonMapper.toCommonResponse(capdCommon);
    }

    // 날짜 이용한 환자가 작성한 해당 날짜의 특정 세션 투석일지 조회
    public CapdSessionResponse capdSessionRead(Long patientId, LocalDate date, int sessionNumber) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 날짜의 공통 일지를 찾기
        CapdCommonEntity common = capdCommonRepository.findByPatientAndDate(patient, date)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // 공통 투석일지 해당된 특정 회차(1~5) 세션 투석일지을 찾기
        CapdSessionEntity session = capdSessionRepository.findByCapdCommonAndSessionNumber(common, sessionNumber)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        // entity -> dto 변환
        return capdSessionMapper.toSessionResponse(session);
    }

    // 공통 투석일지 고유 ID로 투석일지 조회
    public CapdCommonResponse capdIDCommonRead(Long patientId, Long capdId) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 투석일지 조회
        CapdCommonEntity common = capdCommonRepository.findById(capdId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // 조회된 투석일지가 요청한 환자의 것이 맞는지 확인
        if (!common.getPatient().getPatientId().equals(patientId)) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // entity -> dto
        return capdCommonMapper.toCommonResponse(common);
    }

    // 세션 투석일지 고유 ID(PK)로 조회
    public CapdSessionResponse capdSessionIdRead(Long patientId, Long capdSessionId) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 투석일지 조회
        CapdSessionEntity session = capdSessionRepository.findById(capdSessionId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        // 해당 세션 투석일지의 부모 투석 일지가 요청한 환자의 것이 맞는지 확인
        if (!session.getCapdCommon().getPatient().getPatientId().equals(patientId)) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // entity -> dto
        return capdSessionMapper.toSessionResponse(session);
    }
}

package com.capd.capdbackend.domain.capd.service;

import com.capd.capdbackend.domain.capd.dto.request.CapdCreateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdSessionCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdSessionEntity;
import com.capd.capdbackend.domain.capd.entity.CapdStatus;
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

    // 임시저장
    @Transactional
    public CapdCommonResponse saveCapd(Long patientId, CapdCreateRequest request) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 당일 SUBMITTED 상태면 임시저장 불가
        Optional<CapdCommonEntity> existCapd =
                capdCommonRepository.findByPatientAndDate(patient, request.getDate());

        if (existCapd.isPresent() &&
                existCapd.get().getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        CapdCommonEntity common;

        if (existCapd.isPresent()) {
            // 기존 TEMP 있으면 덮어쓰기
            common = existCapd.get();
            common.updateCommonInfo(request);

            // 기존 세션 전부 지우고 다시 저장
            common.getSessions().clear();
        } else {
            // 없으면 새로 생성
            common = capdCommonMapper.toTempEntity(request, patient);
            capdCommonRepository.save(common);
        }

        // 세션 저장
        saveSessions(request, common);

        return capdCommonMapper.toCommonResponse(common);
    }

    // 최종 제출 (마감하기)
    @Transactional
    public CapdCommonResponse submitCapd(Long patientId, CapdCreateRequest request) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 당일 SUBMITTED 상태면 재제출 불가
        Optional<CapdCommonEntity> existCapd =
                capdCommonRepository.findByPatientAndDate(patient, request.getDate());

        if (existCapd.isPresent() &&
                existCapd.get().getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        CapdCommonEntity common;

        if (existCapd.isPresent()) {
            // 임시저장 있으면 덮어쓰고 SUBMITTED 로 변경
            common = existCapd.get();
            common.updateCommonInfo(request);
            common.getSessions().clear();
        } else {
            // 없으면 새로 생성
            common = capdCommonMapper.toSubmitEntity(request, patient);
            capdCommonRepository.save(common);
        }

        // status SUBMITTED 로 변경
        common.setStatus(CapdStatus.SUBMITTED);

        // 세션 저장
        saveSessions(request, common);

        return capdCommonMapper.toCommonResponse(common);
    }

    // 임시저장 데이터 불러오기
    public CapdCommonResponse getTempCapd(Long patientId, LocalDate date) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        CapdCommonEntity common = capdCommonRepository
                .findByPatientAndDateAndStatus(patient, date, CapdStatus.TEMP)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        return capdCommonMapper.toCommonResponse(common);
    }

    // 세션 저장 공통 메서드 (private)
    private void saveSessions(CapdCreateRequest request, CapdCommonEntity common) {
        if (request.getSessions() != null && !request.getSessions().isEmpty()) {
            for (CapdSessionCreateRequest sessionRequest : request.getSessions()) {
                CapdSessionEntity session = capdSessionMapper.toSessionEntity(sessionRequest, common);
                capdSessionRepository.save(session);
                common.getSessions().add(session);
            }
            common.calculateTotalUltrafiltration();
        }
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

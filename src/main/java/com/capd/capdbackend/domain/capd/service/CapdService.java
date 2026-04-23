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

    // 임시저장 (공통 + 세션 같이 or 세션만)
    @Transactional
    public CapdCommonResponse saveCapd(Long patientId, CapdCreateRequest request) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Optional<CapdCommonEntity> existCapd =
                capdCommonRepository.findByPatientAndDate(patient, request.getDate());

        if (existCapd.isPresent() && existCapd.get().getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        CapdCommonEntity common;

        if (existCapd.isPresent()) {
            common = existCapd.get();
            common.updateCommonInfo(request);
        } else {
            common = capdCommonMapper.toTempEntity(request, patient);
            capdCommonRepository.save(common);
        }

        if (request.getSessions() != null && !request.getSessions().isEmpty()) {
            for (CapdSessionCreateRequest sessionRequest : request.getSessions()) {
                if (capdSessionRepository.existsByCapdCommonAndSessionNumber(
                        common, sessionRequest.getSessionNumber())) {
                    throw new CustomException(CapdErrorCode.ALREADY_EXIST_SESSION);
                }
                CapdSessionEntity session = capdSessionMapper.toSessionEntity(sessionRequest, common);
                capdSessionRepository.save(session);
                common.getSessions().add(session);
            }
            common.calculateTotalUltrafiltration();
        }

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

    // 최종 제출 (마감하기)
    @Transactional
    public CapdCommonResponse submitCapd(Long patientId, CapdCreateRequest request) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Optional<CapdCommonEntity> existCapd =
                capdCommonRepository.findByPatientAndDate(patient, request.getDate());

        // 이미 최종 제출됐으면 재제출 불가
        if (existCapd.isPresent() && existCapd.get().getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        CapdCommonEntity common;

        if (existCapd.isPresent()) {
            // 임시저장 있으면 공통 정보 업데이트 + 세션 전부 지우고 재저장
            common = existCapd.get();
            common.updateCommonInfo(request);

            // DB 삭제
            capdSessionRepository.deleteAll(common.getSessions());
            common.getSessions().clear();
        } else {
            // 임시저장 없이 바로 제출하는 경우
            common = capdCommonMapper.toSubmitEntity(request, patient);
            capdCommonRepository.save(common);
        }

        // SUBMITTED 로 변경
        common.setStatus(CapdStatus.SUBMITTED);

        // 세션 전체 저장
        if (request.getSessions() != null && !request.getSessions().isEmpty()) {
            for (CapdSessionCreateRequest sessionRequest : request.getSessions()) {
                CapdSessionEntity session = capdSessionMapper.toSessionEntity(sessionRequest, common);
                capdSessionRepository.save(session);
                common.getSessions().add(session);
            }
            common.calculateTotalUltrafiltration();
        }

        return capdCommonMapper.toCommonResponse(common);
    }

    // 전체 목록 조회 (환자용 — TEMP 포함)
    public List<CapdCommonResponse> capdAllReadForPatient(Long patientId) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        List<CapdCommonEntity> list =
                capdCommonRepository.findAllByPatientOrderByDateDesc(patient);

        List<CapdCommonResponse> resultList = new ArrayList<>();
        for (CapdCommonEntity capdCommon : list) {
            resultList.add(capdCommonMapper.toCommonResponse(capdCommon));
        }
        return resultList;
    }

    // 날짜로 단건 조회
    public CapdCommonResponse capdProfileRead(Long patientId, LocalDate date) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        CapdCommonEntity capdCommon = capdCommonRepository
                .findByPatientAndDate(patient, date)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        return capdCommonMapper.toCommonResponse(capdCommon);
    }

    // 날짜 + 회차로 특정 세션 조회
    public CapdSessionResponse capdSessionRead(Long patientId, LocalDate date, int sessionNumber) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        CapdCommonEntity common = capdCommonRepository
                .findByPatientAndDate(patient, date)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        CapdSessionEntity session = capdSessionRepository
                .findByCapdCommonAndSessionNumber(common, sessionNumber)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        return capdSessionMapper.toSessionResponse(session);
    }

    // 공통 일지 ID로 단건 조회
    public CapdCommonResponse capdIDCommonRead(Long patientId, Long capdId) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        CapdCommonEntity common = capdCommonRepository.findById(capdId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        if (!common.getPatient().getPatientId().equals(patientId)) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        return capdCommonMapper.toCommonResponse(common);
    }

    // 세션 ID로 단건 조회
    public CapdSessionResponse capdSessionIdRead(Long patientId, Long capdSessionId) {

        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        CapdSessionEntity session = capdSessionRepository.findById(capdSessionId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        if (!session.getCapdCommon().getPatient().getPatientId().equals(patientId)) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        return capdSessionMapper.toSessionResponse(session);
    }
}

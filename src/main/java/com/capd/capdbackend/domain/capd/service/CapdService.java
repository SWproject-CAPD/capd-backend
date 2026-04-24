package com.capd.capdbackend.domain.capd.service;

import com.capd.capdbackend.domain.capd.dto.request.CapdCommonUpdateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdCreateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdSessionCreateRequest;
import com.capd.capdbackend.domain.capd.dto.request.CapdSessionUpdateRequest;
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
    public CapdCommonResponse saveCapd(String email, CapdCreateRequest request) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 날짜에 작성된 투석일지가 있는지 확인
        Optional<CapdCommonEntity> existCapd = capdCommonRepository.findByPatientAndDate(patient, request.getDate());

        // 최종 제출 상태면 수정 불가
        if (existCapd.isPresent() && existCapd.get().getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        CapdCommonEntity common;

        // 기존 임시저장 데이터가 있으면 내용 업데이트
        if (existCapd.isPresent()) {
            common = existCapd.get();
            common.updateCommonInfo(request);
        }
        // 기존 임시 저장 데이터가 없으면 새로 생성 db 저장
        else {
            common = capdCommonMapper.toTempEntity(request, patient);
            capdCommonRepository.save(common);
        }

        if (request.getSessions() != null && !request.getSessions().isEmpty()) {
            for (CapdSessionCreateRequest sessionRequest : request.getSessions()) {

                // 해당 회차 번호가 이미 존재하는지 확인
                if (capdSessionRepository.existsByCapdCommonAndSessionNumber(common, sessionRequest.getSessionNumber())) {
                    throw new CustomException(CapdErrorCode.ALREADY_EXIST_SESSION);
                }
                CapdSessionEntity session = capdSessionMapper.toSessionEntity(sessionRequest, common);
                capdSessionRepository.save(session);
                common.getSessions().add(session);
            }

            // 총 초여과량 다시 계산
            common.calculateTotalUltrafiltration();
        }

        // entity -> dto
        return capdCommonMapper.toCommonResponse(common);
    }

    // 임시저장 데이터 불러오기
    public CapdCommonResponse getTempCapd(String email, LocalDate date) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 날짜이면서 상태가 임시저장인 투석일지 조회
        CapdCommonEntity common = capdCommonRepository.findByPatientAndDateAndStatus(patient, date, CapdStatus.TEMP)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // entity -> dto
        return capdCommonMapper.toCommonResponse(common);
    }

    // 최종 제출 (마감하기)
    @Transactional
    public CapdCommonResponse submitCapd(String email, CapdCreateRequest request) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 날짜 투석일지 조회
        Optional<CapdCommonEntity> existCapd = capdCommonRepository.findByPatientAndDate(patient, request.getDate());

        // 이미 최종 제출됐으면 재제출 불가
        if (existCapd.isPresent() && existCapd.get().getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        CapdCommonEntity common;

        // 기존 임시저장 투석일지 데이터가 있으면 덮어쓰기
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

        // 제출 완료로 변경
        common.setStatus(CapdStatus.SUBMITTED);

        // 세션 전체 저장
        if (request.getSessions() != null && !request.getSessions().isEmpty()) {
            for (CapdSessionCreateRequest sessionRequest : request.getSessions()) {
                CapdSessionEntity session = capdSessionMapper.toSessionEntity(sessionRequest, common);
                capdSessionRepository.save(session);
                common.getSessions().add(session);
            }

            // 총 초여과량 다시 계산
            common.calculateTotalUltrafiltration();
        }

        // entity -> dto
        return capdCommonMapper.toCommonResponse(common);
    }

    // 전체 목록 조회
    public List<CapdCommonResponse> capdAllReadForPatient(String email) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 투석 일지를 최신순으로 조회
        List<CapdCommonEntity> list = capdCommonRepository.findAllByPatientOrderByDateDesc(patient);

        // entity 리스트 -> dto 리스트로 변환
        List<CapdCommonResponse> resultList = new ArrayList<>();
        for (CapdCommonEntity capdCommon : list) {
            resultList.add(capdCommonMapper.toCommonResponse(capdCommon));
        }

        // 응답 반환
        return resultList;
    }

    // 날짜로 단건 조회
    public CapdCommonResponse capdProfileRead(String email, LocalDate date) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 날짜를 기준으로 해당 투석일지 조회
        CapdCommonEntity capdCommon = capdCommonRepository.findByPatientAndDate(patient, date)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // entity -> dto
        return capdCommonMapper.toCommonResponse(capdCommon);
    }

    // 날짜 + 회차로 특정 세션 조회
    public CapdSessionResponse capdSessionRead(String email, LocalDate date, int sessionNumber) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 날짜로 공통 투석일지 조회
        CapdCommonEntity common = capdCommonRepository.findByPatientAndDate(patient, date)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // 찾아낸 공통 투석일지와 회차 세션 투석일지 번호로 특정 세션 투석일지 조회
        CapdSessionEntity session = capdSessionRepository.findByCapdCommonAndSessionNumber(common, sessionNumber)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        // entity -> dto
        return capdSessionMapper.toSessionResponse(session);
    }

    // 공통 일지 ID로 단건 조회
    public CapdCommonResponse capdIDCommonRead(String email, Long capdId) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 공통 투석일지 id로 조회
        CapdCommonEntity common = capdCommonRepository.findById(capdId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // 조회하는 투석일지 작성자와 요청한 환자가 일치하는지 확인
        if (!common.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // entity -> dto
        return capdCommonMapper.toCommonResponse(common);
    }

    // 세션 ID로 단건 조회
    public CapdSessionResponse capdSessionIdRead(String email, Long capdSessionId) {

        // 환자 유저 확인
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 세션 투석일지 id로 조회
        CapdSessionEntity session = capdSessionRepository.findById(capdSessionId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        // 해당 세션 투석일지의 주인이 본인이 맞는지 확인
        if (!session.getCapdCommon().getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // entity -> dto
        return capdSessionMapper.toSessionResponse(session);
    }

    // 세션 투석일지 삭제
    @Transactional
    public void deleteCapdSession(String email, Long sessionId) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 세션 투석일지 조회
        CapdSessionEntity capdSession = capdSessionRepository.findByCapdSessionId(sessionId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        // 권한 확인
        CapdCommonEntity common = capdSession.getCapdCommon();
        if (!common.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // 부모 리스트에서 삭제
        common.getSessions().remove(capdSession);

        // db에서 삭제
        capdSessionRepository.delete(capdSession);

        // 총초여과량 다시 계산
        common.calculateTotalUltrafiltration();
    }

    // 투석일지 삭제
    @Transactional
    public void deleteCapdCommon(String email, Long capdId) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 투석일지 조회
        CapdCommonEntity capdCommon = capdCommonRepository.findByCapdId(capdId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // 권한 체크
        if (!capdCommon.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // 삭제
        capdCommonRepository.delete(capdCommon);
    }

    // 공통 투석일지 수정
    @Transactional
    public CapdCommonResponse updateCapdCommon(String email, Long capdId, CapdCommonUpdateRequest request) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 공통 일지 조회
        CapdCommonEntity capdCommon = capdCommonRepository.findByCapdId(capdId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_NOT_FOUND));

        // 본인의 공통 투석일지인지 확인
        if (!capdCommon.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // 제출 상태면 수정 불가
        if (capdCommon.getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        // 공통 정보 수정
        capdCommon.updateCommonInfoFromRequest(request);

        // 로그 출력
        log.info("투석일지 수정 완료: capd={}", capdId);

        // entity -> dto
        return capdCommonMapper.toCommonResponse(capdCommon);}

    // 세션 투석일지 수정
    @Transactional
    public CapdSessionResponse updateCapdSession(String email, Long sessionId, CapdSessionUpdateRequest request) {

        // 환자 유저 확인
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 세션 투석일지 조회
        CapdSessionEntity capdSession = capdSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(CapdErrorCode.CAPD_SESSION_NOT_FOUND));

        // 본인의 세션 투석일지인지 확인
        if (!capdSession.getCapdCommon().getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(PatientErrorCode.PATIENT_NO_PERMISSION);
        }

        // 투석일지가 제출 상태이면 수정 불가
        if (capdSession.getCapdCommon().getStatus() == CapdStatus.SUBMITTED) {
            throw new CustomException(CapdErrorCode.ALREADY_SUBMITTED);
        }

        // 세션 투석일지 수정
        capdSession.updateSessionInfo(request);

        // 총초여과량 재계산
        capdSession.getCapdCommon().calculateTotalUltrafiltration();

        // 로그 출력
        log.info("세션 투석일지 수정 완료: sessionId={}", sessionId);

        // entity -> dto
        return capdSessionMapper.toSessionResponse(capdSession);
    }
}

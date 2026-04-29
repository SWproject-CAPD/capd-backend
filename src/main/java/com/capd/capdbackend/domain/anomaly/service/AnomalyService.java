package com.capd.capdbackend.domain.anomaly.service;

import com.capd.capdbackend.domain.anomaly.client.MlApiClient;
import com.capd.capdbackend.domain.anomaly.dto.response.AnomalyResultResponse;
import com.capd.capdbackend.domain.anomaly.entity.AnomalyResultEntity;
import com.capd.capdbackend.domain.anomaly.repository.AnomalyResultRepository;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdStatus;
import com.capd.capdbackend.domain.capd.exception.CapdErrorCode;
import com.capd.capdbackend.domain.capd.repository.CapdCommonRepository;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.exception.DoctorErrorCode;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnomalyService {

    private final AnomalyResultRepository anomalyResultRepository;
    private final CapdCommonRepository capdCommonRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MlApiClient mlApiClient;
    private final ObjectMapper objectMapper;

    // 저장된 이상치 결과 전체 조회
    public List<AnomalyResultResponse> getAnomalyResults(String licenseId, Long patientId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자가 담당 환자인지 확인
        if (patient.getDoctor() == null || !patient.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        // 응답 반환
        return anomalyResultRepository.findAllByPatientOrderByAnalysisDateDesc(patient)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // 의사가 특정 날짜 분석 요청
    @Transactional
    public AnomalyResultResponse analyzePatient(
            String licenseId, Long patientId, LocalDate date) {

        // 의사 확인
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 확인
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 담당 환자인지 확인
        if (patient.getDoctor() == null || !patient.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        // 최근 7일치 SUBMITTED 기록 조회
        List<CapdCommonEntity> recentRecords = capdCommonRepository
                .findTop7ByPatientAndStatusOrderByDateDesc(patient, CapdStatus.SUBMITTED);

        if (recentRecords.isEmpty()) {
            throw new CustomException(CapdErrorCode.CAPD_NOT_FOUND);
        }

        // 날짜 오름차순 정렬 (오래된 것 먼저 — FastAPI에서 rolling 계산에 필요)
        recentRecords.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        // FastAPI 호출
        Map<String, Object> result = mlApiClient.requestAnomalyAnalysis(
                String.valueOf(patient.getPatientId()), recentRecords);

        // top_causes JSON 변환
        String topCausesJson = "";
        try {
            topCausesJson = objectMapper.writeValueAsString(result.get("top_causes"));
        } catch (Exception e) {
            log.error("top_causes JSON 변환 실패");
        }

        // 같은 날짜 결과가 있으면 업데이트, 없으면 새로 저장
        AnomalyResultEntity anomalyResult = anomalyResultRepository
                .findByPatientAndAnalysisDate(patient, date)
                .orElse(AnomalyResultEntity.builder()
                        .patient(patient)
                        .analysisDate(date)
                        .riskLevel(0)
                        .anomalyScore(0f)
                        .statusMessage("")
                        .topCauses("")
                        .build());

        anomalyResult.updateResult(
                (Integer) result.get("risk_level"),
                ((Number) result.get("anomaly_score")).floatValue(),
                (String) result.get("status_message"),
                topCausesJson
        );

        anomalyResultRepository.save(anomalyResult);
        log.info("이상치 분석 완료: patientId={}, riskLevel={}",
                patientId, result.get("risk_level"));

        return toResponse(anomalyResult);
    }

    // Entity → Response DTO 변환
    private AnomalyResultResponse toResponse(AnomalyResultEntity entity) {
        return AnomalyResultResponse.builder()
                .anomalyId(entity.getAnomalyId())
                .analysisDate(entity.getAnalysisDate())
                .riskLevel(entity.getRiskLevel())
                .anomalyScore(entity.getAnomalyScore())
                .statusMessage(entity.getStatusMessage())
                .topCauses(entity.getTopCauses())
                .build();
    }
}

package com.capd.capdbackend.domain.report.service;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdStatus;
import com.capd.capdbackend.domain.capd.repository.CapdCommonRepository;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.exception.DoctorErrorCode;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.report.client.GeminiApiClient;
import com.capd.capdbackend.domain.report.dto.response.ReportCreateResponse;
import com.capd.capdbackend.domain.report.entity.ReportEntity;
import com.capd.capdbackend.domain.report.exception.ReportErrorCode;
import com.capd.capdbackend.domain.report.mapper.ReportMapper;
import com.capd.capdbackend.domain.report.repository.ReportRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final CapdCommonRepository capdCommonRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final GeminiApiClient geminiApiClient;
    private final ReportMapper reportMapper;

    // 주간 보고서 생성 (월 + 주차 선택)
    @Transactional
    public ReportCreateResponse generateReport(
            String licenseId, Long patientId,
            int year, int month, int weekNumber) {

        // 의사 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 담당 환자인지 확인
        if (patient.getDoctor() == null ||
                !patient.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        // 주차 → 날짜 범위 계산
        LocalDate[] dateRange = getWeekDateRange(year, month, weekNumber);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        // 같은 기간 보고서 이미 존재하면 오류
        if (reportRepository.findByDoctorAndPatientAndStartDateAndEndDate(
                doctor, patient, startDate, endDate).isPresent()) {
            throw new CustomException(ReportErrorCode.REPORT_ALREADY_EXISTS);
        }

        // 해당 기간 투석 데이터 조회
        List<CapdCommonEntity> records = capdCommonRepository
                .findAllByPatientAndStatusAndDateBetweenOrderByDateAsc(
                        patient, CapdStatus.SUBMITTED, startDate, endDate);

        // 투석 데이터 없으면 오류
        if (records.isEmpty()) {
            throw new CustomException(ReportErrorCode.REPORT_NO_DATA);
        }

        // 통계 계산
        double avgWeight = records.stream()
                .mapToDouble(CapdCommonEntity::getBodyWeight).average().orElse(0);
        double minWeight = records.stream()
                .mapToDouble(CapdCommonEntity::getBodyWeight).min().orElse(0);
        double maxWeight = records.stream()
                .mapToDouble(CapdCommonEntity::getBodyWeight).max().orElse(0);

        double avgSysBp = records.stream()
                .mapToDouble(CapdCommonEntity::getBloodPressureSys).average().orElse(0);
        double avgDiaBp = records.stream()
                .mapToDouble(CapdCommonEntity::getBloodPressureDia).average().orElse(0);

        double avgBloodSugar = records.stream()
                .mapToDouble(CapdCommonEntity::getFastingBloodSugar).average().orElse(0);

        double avgUF = records.stream()
                .mapToDouble(CapdCommonEntity::getTotalUltrafiltration).average().orElse(0);
        double minUF = records.stream()
                .mapToDouble(CapdCommonEntity::getTotalUltrafiltration).min().orElse(0);
        double maxUF = records.stream()
                .mapToDouble(CapdCommonEntity::getTotalUltrafiltration).max().orElse(0);

        // 요약 텍스트 생성
        String weightSummary = String.format(
                "평균 %.1fkg (최소 %.1fkg, 최대 %.1fkg)",
                avgWeight, minWeight, maxWeight);
        String bpSummary = String.format(
                "평균 수축기 %.0fmmHg / 이완기 %.0fmmHg",
                avgSysBp, avgDiaBp);
        String bloodSugarSummary = String.format(
                "평균 공복혈당 %.0fmg/dL", avgBloodSugar);
        String ufSummary = String.format(
                "평균 총초여과량 %.0fg (최소 %.0fg, 최대 %.0fg)",
                avgUF, minUF, maxUF);

        long cloudyCount = records.stream()
                .filter(CapdCommonEntity::isCloudyDialysate).count();
        String anomalySummary = cloudyCount > 0
                ? String.format("배액 혼탁 %d회 발생", cloudyCount)
                : "특이 이상치 없음";

        // Gemini 프롬프트 구성 및 호출
        String prompt = buildPrompt(
                patient.getUser().getUserName(),
                startDate, endDate,
                weightSummary, bpSummary,
                bloodSugarSummary, ufSummary,
                anomalySummary, records.size()
        );
        String docSummary = geminiApiClient.generateContent(prompt);

        // 보고서 저장
        ReportEntity report = ReportEntity.builder()
                .doctor(doctor)
                .patient(patient)
                .startDate(startDate)
                .endDate(endDate)
                .weightSummary(weightSummary)
                .bpSummary(bpSummary)
                .bloodSugarSummary(bloodSugarSummary)
                .ufSummary(ufSummary)
                .anomalySummary(anomalySummary)
                .docSummary(docSummary)
                .build();

        reportRepository.save(report);
        log.info("주간 보고서 생성 완료: patientId={}, {}년 {}월 {}주차 ({}~{})",
                patientId, year, month, weekNumber, startDate, endDate);

        return reportMapper.toResponse(report);
    }

    // 저장된 보고서 전체 조회
    public List<ReportCreateResponse> getReports(
            String licenseId, Long patientId) {

        // 의사 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 담당 환자인지 확인
        if (patient.getDoctor() == null ||
                !patient.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        return reportRepository
                .findAllByDoctorAndPatientOrderByStartDateDesc(doctor, patient)
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    // 주차 → 날짜 범위 계산
    private LocalDate[] getWeekDateRange(int year, int month, int weekNumber) {

        // 해당 월의 1일
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);

        // 주차 기준 시작일 계산
        LocalDate startDate = firstDayOfMonth.plusDays((long) (weekNumber - 1) * 7);

        // 해당 월의 마지막 날
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(
                firstDayOfMonth.lengthOfMonth());

        // 종료일 = 시작일 + 6일 (월의 마지막 날 초과하면 마지막 날로 설정)
        LocalDate endDate = startDate.plusDays(6);
        if (endDate.isAfter(lastDayOfMonth)) {
            endDate = lastDayOfMonth;
        }

        return new LocalDate[]{startDate, endDate};
    }

    // Gemini 프롬프트 구성
    private String buildPrompt(
            String patientName, LocalDate startDate, LocalDate endDate,
            String weightSummary, String bpSummary,
            String bloodSugarSummary, String ufSummary,
            String anomalySummary, int totalDays) {

        return String.format("""
                당신은 CAPD(복막투석) 전문 의료 AI 어시스턴트입니다.
                아래는 %s 환자의 %s ~ %s (%d일간) 투석 데이터 요약입니다.
                                
                [체중 변화]
                %s
                                
                [혈압 변화]
                %s
                                
                [혈당 변화]
                %s
                                
                [총초여과량 변화]
                %s
                                
                [이상치 발생]
                %s
                                
                위 데이터를 바탕으로 다음 내용을 포함한 종합 소견을 한국어로 작성해주세요:
                1. 전반적인 투석 상태 평가
                2. 주목해야 할 수치 변화
                3. 의사에게 권고하는 주의사항
                                
                소견은 전문적이지만 이해하기 쉽게 3~5문장으로 작성해주세요.
                """,
                patientName, startDate, endDate, totalDays,
                weightSummary, bpSummary,
                bloodSugarSummary, ufSummary,
                anomalySummary
        );
    }
}

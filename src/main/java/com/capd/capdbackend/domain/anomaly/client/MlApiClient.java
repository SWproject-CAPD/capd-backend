package com.capd.capdbackend.domain.anomaly.client;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MlApiClient {

    private final RestClient restClient;

    // application.yml(또는 properties)에서 URL을 주입받음. 없으면 localhost 기본값 사용
    public MlApiClient(@Value("${ml.api.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Map<String, Object> requestAnomalyAnalysis(
            String patientId, List<CapdCommonEntity> recentRecords) {

        // 1. 요청 데이터 구성 (멘토님 코드 그대로!)
        List<Map<String, Object>> records = recentRecords.stream()
                .map(r -> Map.<String, Object>of(
                        "date", r.getDate().toString(),
                        "body_weight_kg", (double) r.getBodyWeight(),
                        "systolic_bp_mmhg", (double) r.getBloodPressureSys(),
                        "diastolic_bp_mmhg", (double) r.getBloodPressureDia(),
                        "fasting_blood_sugar_mg_dl", (double) r.getFastingBloodSugar(),
                        "total_ultrafiltration_g", (double) r.getTotalUltrafiltration(),
                        "urination_count", r.getUrinationCount()
                ))
                .toList();

        Map<String, Object> requestBody = Map.of(
                "patient_id", patientId,
                "records", records
        );

        try {
            // 2. RestClient를 이용한 동기식 통신 (.block()이 필요 없음!)
            Map response = restClient.post()
                    .uri("/api/ml/anomaly")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class); // Map 형태로 바로 받아옴

            log.info("ML 분석 완료: patient_id={}, risk_level={}",
                    patientId, response.get("risk_level"));
            return response;

        } catch (Exception e) {
            log.error("ML API 호출 실패: {}", e.getMessage());
            // 3. 프로젝트의 규격화된 커스텀 에러로 던지기 (예시)
            // throw new CustomException(ErrorCode.ML_SERVER_ERROR);
            throw new RuntimeException("ML 서버 연결 실패: " + e.getMessage());
        }
    }
}
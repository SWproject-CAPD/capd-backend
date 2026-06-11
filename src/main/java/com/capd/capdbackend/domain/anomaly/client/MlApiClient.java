package com.capd.capdbackend.domain.anomaly.client;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MlApiClient {

    private final RestClient restClient;

    // application.yml(또는 properties)에서 URL을 주입, 없으면 localhost 기본값 사용
    public MlApiClient(@Value("${ml.api.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .build();
    }

    public Map<String, Object> requestAnomalyAnalysis(
            String patientId, List<CapdCommonEntity> recentRecords) {

        // 요청 데이터 구성
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
            // RestClient를 이용한 통신
            Map response = restClient.post()
                    .uri("/api/ml/anomaly")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class); // Map 형태로 바로 받아옴

            log.info("ML 분석 완료: patient_id={}, risk_level={}",
                    patientId, response.get("risk_level"));
            return response;

        } catch (Exception e) {
            log.error("ML API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("ML 서버 연결 실패: " + e.getMessage());
        }
    }
}
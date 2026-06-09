package com.capd.capdbackend.domain.chat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ChatApiClient {

    private final RestClient restClient;

    public ChatApiClient(@Value("${ml.api.base-url}") String mlApiBaseUrl) {
        this.restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .baseUrl(mlApiBaseUrl)
                .build();
    }

    public String chat(String userType, String userMessage, String patientName, List<Map<String, Object>> recentRecords) {
        try {

            // JSON 구성
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> patientData = new HashMap<>(); // HashMap으로 중첩 구조 생성
            patientData.put("patient_name", patientName);
            patientData.put("recent_records", recentRecords);

            Map<String, Object> requestBody = new HashMap<>(); // 중첩 구조 생성
            requestBody.put("user_type", userType);
            requestBody.put("user_message", userMessage);
            requestBody.put("patient_data", patientData);

            // JSON 문자열로 변환
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 로그 출력
            log.info("FastAPI 요청 body: {}", requestBody);

            // HTTP 요청 전송
            Map response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return (String) response.get("ai_answer");

        }
        catch (Exception e) {
            log.error("챗봇 API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("챗봇 서버 연결 실패: " + e.getMessage());
        }
    }
}
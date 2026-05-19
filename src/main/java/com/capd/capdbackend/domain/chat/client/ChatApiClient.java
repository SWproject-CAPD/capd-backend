package com.capd.capdbackend.domain.chat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    public ChatApiClient() {
        this.restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())  // ← 이 줄 추가
                .baseUrl("http://localhost:8000")
                .build();
    }


    public String chat(String userType, String userMessage,
                       String patientName, List<Map<String, Object>> recentRecords) {
        try {
            // JSON 문자열 변환
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> patientData = new HashMap<>();
            patientData.put("patient_name", patientName);
            patientData.put("recent_records", recentRecords);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_type", userType);
            requestBody.put("user_message", userMessage);
            requestBody.put("patient_data", patientData);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.info("FastAPI 요청 body: {}", requestBody);

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

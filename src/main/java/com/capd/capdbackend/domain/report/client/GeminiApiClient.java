package com.capd.capdbackend.domain.report.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GeminiApiClient {

    private final RestClient restClient;
    private final String model;

    public GeminiApiClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model) {

        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    public String generateContent(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            Map response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // 응답에서 텍스트 추출
            List<Map> candidates = (List<Map>) response.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            log.info("Gemini 응답 완료");
            return text;

        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("Gemini 서버 연결 실패: " + e.getMessage());
        }
    }

}

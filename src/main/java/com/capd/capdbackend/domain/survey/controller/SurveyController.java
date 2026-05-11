package com.capd.capdbackend.domain.survey.controller;

import com.capd.capdbackend.domain.survey.dto.response.QuestionResponse;
import com.capd.capdbackend.domain.survey.service.SurveyService;
import com.capd.capdbackend.global.response.BaseResponse;
import com.capd.capdbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Survey", description = "AI 설문 관련 API")
public class SurveyController {

    private final SurveyService surveyService;

    // 의사가 질문 생성
    @Operation(summary = "AI 질문 생성", description = "의사가 특정 예약에 대한 AI 질문 1개를 생성하는 API")
    @PostMapping("/surveys/{reservationId}/questions")
    public ResponseEntity<BaseResponse<QuestionResponse>> generateQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId) {

        // service 호출
        QuestionResponse response = surveyService.createQuestion(userDetails.getIdentifier(), reservationId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "질문 생성 성공", response));
    }
}

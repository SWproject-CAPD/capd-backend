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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Survey", description = "AI 설문 관련 API")
public class SurveyController {

    private final SurveyService surveyService;

    // 의사가 질문 생성
    @Operation(summary = "AI 질문 생성", description = "의사가 특정 예약에 대한 AI 질문 1개를 생성하는 API")
    @PostMapping("/surveys/{reservationId}/questions")
    public ResponseEntity<BaseResponse<QuestionResponse>> createQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId) {

        // service 호출
        QuestionResponse response = surveyService.createQuestion(userDetails.getIdentifier(), reservationId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "질문 생성 성공", response));
    }

    // 의사가 질문 승인
    @Operation(summary = "의사가 질문 승인", description = "의사가 AI가 생성해준 질문을 승인하는 API")
    @PatchMapping("/surveys/questions/{questionId}/approve")
    public ResponseEntity<BaseResponse<QuestionResponse>> approveQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId) {

        // service 호출
        QuestionResponse response = surveyService.approveQuestion(userDetails.getIdentifier(), questionId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200 ,"질문 승인 성공", response));
    }

    // 의사가 질문 거절
    @Operation(summary = "의사가 질문 거절", description = "의사가 AI가 생성해준 질문을 거절하는 API")
    @PatchMapping("/surveys/questions/{questionId}/reject")
    public ResponseEntity<BaseResponse<QuestionResponse>> rejectQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId) {

        // service 호출
        QuestionResponse response = surveyService.rejectQuestion(userDetails.getIdentifier(), questionId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "질문 거절 성공", response));
    }
}

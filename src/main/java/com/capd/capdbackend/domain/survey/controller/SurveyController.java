package com.capd.capdbackend.domain.survey.controller;

import com.capd.capdbackend.domain.survey.dto.request.AnswerListRequest;
import com.capd.capdbackend.domain.survey.dto.request.PassiveQuestionRequest;
import com.capd.capdbackend.domain.survey.dto.response.AnswerResponse;
import com.capd.capdbackend.domain.survey.dto.response.PatientQuestionResponse;
import com.capd.capdbackend.domain.survey.dto.response.QuestionResponse;
import com.capd.capdbackend.domain.survey.service.SurveyService;
import com.capd.capdbackend.global.response.BaseResponse;
import com.capd.capdbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 환자가 승인된 질문 조회
    @Operation(summary = "환자가 승인된 질문을 조회", description = "환자가 의사가 승인한 질문을 예약 날짜 전날까지 조회하는 API")
    @GetMapping("/surveys/{reservationId}/patient/questions")
    public ResponseEntity<BaseResponse<List<PatientQuestionResponse>>> checkQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId) {

        // service 호출
        List<PatientQuestionResponse> response = surveyService.checkQuestion(userDetails.getIdentifier(), reservationId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "질문 조회 성공", response));
    }

    // 환자가 승인된 질문 답변
    @Operation(summary = "설문 답변 제출", description = "환자가 승인된 질문에 대한 답변을 한 번에 제출하는 API")
    @PostMapping("/surveys/{reservationId}/answers")
    public ResponseEntity<BaseResponse<List<AnswerResponse>>> submitAnswers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId,
            @RequestBody @Valid AnswerListRequest request) {

        // service 호출
        List<AnswerResponse> response = surveyService.answerQuestion(userDetails.getIdentifier(), reservationId, request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "답변 제출 성공", response));
    }

    // 의사가 특정 예약을 기준으로 질문 조회
    @Operation(summary = "의사용 질문 목록 조회", description = "의사가 특정 예약의 질문 목록을 조회하는 API")
    @GetMapping("/surveys/{reservationId}/questions")
    public ResponseEntity<BaseResponse<List<QuestionResponse>>> getDoctorQuestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId) {

        // service 호출
        List<QuestionResponse> response = surveyService.checkReservationQuestion(userDetails.getIdentifier(), reservationId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "질문 목록 조회 성공", response));
    }

    // 의사가 특정 예약의 답변 목록 조회
    @Operation(summary = "의사 답변 목록 조회", description = "의사가 특정 예약의 환자 답변 목록을 조회하는 API")
    @GetMapping("/surveys/{reservationId}/answers")
    public ResponseEntity<BaseResponse<List<AnswerResponse>>> getDoctorAnswers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId) {

        // service 호출
        List<AnswerResponse> response = surveyService.checkAnswer(userDetails.getIdentifier(), reservationId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "답변 목록 조회 성공", response));
    }

    // 질문 상태 되돌리기
    @Operation(summary = "질문 상태 되돌리기", description = "의사가 승인/거절한 질문을 다시 초기 상태로 되돌리는 API")
    @PatchMapping("/surveys/questions/{questionId}/reset")
    public ResponseEntity<BaseResponse<QuestionResponse>> resetQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId) {

        // service 호출
        QuestionResponse response = surveyService.resetQuestion(userDetails.getIdentifier(), questionId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "질문 상태 되돌리기 성공", response));
    }

    // 질문 의미 ai 요청
    @Operation(summary = "AI에게 질문 해석 요청", description = "환자가 질문이 이해가 가지 않을때 AI에게 해석을 요청해 질문의 의도를 파악하는 API")
    @PostMapping("/surveys/questions/{questionId}/explain")
    public ResponseEntity<BaseResponse<String>> explainQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId) {

        // service 호출
        String response = surveyService.explainQuestion(userDetails.getIdentifier(), questionId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "AI 설명 생성 성공", response));
    }

    // 환자가 본인의 답변 목록 조회
    @Operation(summary = "환자 답변 목록 조회", description = "환자가 제출한 답변 목록을 조회하는 API")
    @GetMapping("/surveys/{reservationId}/patient/answers")
    public ResponseEntity<BaseResponse<List<AnswerResponse>>> getPatientAnswers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId) {

        // service 호출
        List<AnswerResponse> response = surveyService.getPatientAnswers(userDetails.getIdentifier(), reservationId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "답변 목록 조회 성공", response));
    }

    // 환자가 본인이 작성한 답변 조회
    @Operation(summary = "환자 단건 답변 조회", description = "환자가 특정 답변을 조회하는 API")
    @GetMapping("/surveys/answers/{answerId}/patient")
    public ResponseEntity<BaseResponse<AnswerResponse>> getPatientAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId) {

        // service 호출
        AnswerResponse response = surveyService.getPatientAnswer(userDetails.getIdentifier(), answerId);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "답변 조회 성공", response));
    }

    // 의사가 수동으로 질문 생성
    @Operation(summary = "수동 질문 생성", description = "의사가 직접 질문을 생성하는 API")
    @PostMapping("/surveys/{reservationId}/questions/manual")
    public ResponseEntity<BaseResponse<QuestionResponse>> createManualQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId,
            @RequestBody @Valid PassiveQuestionRequest request) {

        // service 호출
        QuestionResponse response = surveyService.createPassiveQuestion(userDetails.getIdentifier(), reservationId, request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "수동 질문 생성 성공", response));
    }
}

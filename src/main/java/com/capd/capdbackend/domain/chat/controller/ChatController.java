package com.capd.capdbackend.domain.chat.controller;

import com.capd.capdbackend.domain.chat.dto.request.ChatRequest;
import com.capd.capdbackend.domain.chat.dto.response.ChatResponse;
import com.capd.capdbackend.domain.chat.service.ChatService;
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
@Tag(name = "Chat", description = "챗봇 관련 API")
public class ChatController {

    private final ChatService chatService;

    // 환자가 챗봇에게 질문하는 API
    @Operation(summary = "환자 챗봇 질문", description = "환자가 KDIGO 기반 AI 챗봇에게 질문하는 API")
    @PostMapping("/chat/patient")
    public ResponseEntity<BaseResponse<ChatResponse>> patientChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ChatRequest request) {

        // service 호출
        ChatResponse response = chatService.patientChat(userDetails.getIdentifier(), request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "챗봇 응답 성공", response));
    }

    // 의사가 챗봇에게 질문하는 API
    @Operation(summary = "의사 챗봇 질문", description = "의사가 특정 환자에 대해 KDIGO 기반 AI 챗봇에게 질문하는 API")
    @PostMapping("/chat/doctor/{patientId}")
    public ResponseEntity<BaseResponse<ChatResponse>> doctorChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long patientId,
            @RequestBody @Valid ChatRequest request) {

        // service 호출
        ChatResponse response = chatService.doctorChat(userDetails.getIdentifier(), patientId, request);

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(201, "챗봇 응답 성공", response));
    }

    // 환자 채팅 기록 조회하는 API
    @Operation(summary = "환자 채팅 기록 조회", description = "환자의 전체 채팅 기록을 조회하는 API")
    @GetMapping("/chat/patient")
    public ResponseEntity<BaseResponse<List<ChatResponse>>> getPatientChatHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        List<ChatResponse> response = chatService.getPatientChatHistory(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "채팅 기록 조회 성공", response));
    }

    // 의사 채팅 기록 조회하는 API
    @Operation(summary = "의사 채팅 기록 조회", description = "의사의 전체 채팅 기록을 조회하는 API")
    @GetMapping("/chat/doctor")
    public ResponseEntity<BaseResponse<List<ChatResponse>>> getDoctorChatHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // service 호출
        List<ChatResponse> response = chatService.getDoctorChatHistory(userDetails.getIdentifier());

        // 응답 반환
        return ResponseEntity.ok(BaseResponse.success(200, "채팅 기록 조회 성공", response));
    }
}

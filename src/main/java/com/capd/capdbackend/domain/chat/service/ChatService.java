package com.capd.capdbackend.domain.chat.service;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdStatus;
import com.capd.capdbackend.domain.capd.repository.CapdCommonRepository;
import com.capd.capdbackend.domain.chat.client.ChatApiClient;
import com.capd.capdbackend.domain.chat.dto.request.ChatRequest;
import com.capd.capdbackend.domain.chat.dto.response.ChatResponse;
import com.capd.capdbackend.domain.chat.entity.ChatLogEntity;
import com.capd.capdbackend.domain.chat.mapper.ChatMapper;
import com.capd.capdbackend.domain.chat.repository.ChatLogRepository;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatService {

    private final ChatLogRepository chatLogRepository;
    private final CapdCommonRepository capdCommonRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ChatApiClient chatApiClient;
    private final ChatMapper chatMapper;

    // 환자가 AI 챗봇에게 질문
    @Transactional
    public ChatResponse patientChat(String email, ChatRequest request) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 최근 투석일지 7개 조회
        List<CapdCommonEntity> records = capdCommonRepository.findTop7ByPatientAndStatusOrderByDateDesc(patient, CapdStatus.SUBMITTED);

        // 오름차순 정렬
        records.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        // 투석 데이터 변환
        List<Map<String, Object>> recentRecords = records.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", r.getDate().toString());
                    map.put("body_weight_kg", r.getBodyWeight());
                    map.put("systolic_bp_mmhg", r.getBloodPressureSys());
                    map.put("diastolic_bp_mmhg", r.getBloodPressureDia());
                    map.put("fasting_blood_sugar", r.getFastingBloodSugar());
                    map.put("total_ultrafiltration", r.getTotalUltrafiltration());
                    return map;
                })
                .toList();

        // FastAPI 챗봇 호출
        String aiAnswer = chatApiClient.chat("PATIENT", request.getUserText(), patient.getUser().getUserName(), recentRecords);

        // 메시지 표시 순서 계산
        int displayOrder = chatLogRepository.countByPatient(patient) + 1;

        // ChatLog 저장
        ChatLogEntity chatLog = ChatLogEntity.builder()
                .patient(patient)
                .displayOrder(displayOrder)
                .userText(request.getUserText())
                .aiText(aiAnswer)
                .build();

        // DB 저장
        chatLogRepository.save(chatLog);

        // 로그 출력
        log.info("환자 챗봇 대화 저장 완료: patientId={}", patient.getPatientId());

        // entity -> dto
        return chatMapper.toResponse(chatLog);
    }

    // 의사가 AI 챗봇에게 질문
    @Transactional
    public ChatResponse doctorChat(String licenseId, Long patientId, ChatRequest request) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 최근 투석일지 7개 조회
        List<CapdCommonEntity> records = capdCommonRepository.findTop7ByPatientAndStatusOrderByDateDesc(patient, CapdStatus.SUBMITTED);

        // 오름차순 정렬
        records.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        // 투석 데이터 변환
        List<Map<String, Object>> recentRecords = records.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", r.getDate().toString());
                    map.put("body_weight_kg", r.getBodyWeight());
                    map.put("systolic_bp_mmhg", r.getBloodPressureSys());
                    map.put("diastolic_bp_mmhg", r.getBloodPressureDia());
                    map.put("fasting_blood_sugar", r.getFastingBloodSugar());
                    map.put("total_ultrafiltration", r.getTotalUltrafiltration());
                    return map;
                })
                .toList();

        // FastAPI 챗봇 호출
        String aiAnswer = chatApiClient.chat(
                "DOCTOR",
                request.getUserText(),
                patient.getUser().getUserName(),
                recentRecords
        );

        // 메시지 표시 순서 계산
        int displayOrder = chatLogRepository.countByDoctor(doctor) + 1;

        // ChatLog 저장
        ChatLogEntity chatLog = ChatLogEntity.builder()
                .doctor(doctor)
                .displayOrder(displayOrder)
                .userText(request.getUserText())
                .aiText(aiAnswer)
                .build();

        // DB 저장
        chatLogRepository.save(chatLog);

        // 로그 출력
        log.info("의사 챗봇 대화 저장 완료: doctorId={}", doctor.getDoctorId());

        // entity -> dto
        return chatMapper.toResponse(chatLog);
    }

    // 환자 채팅 기록 조회
    public List<ChatResponse> getPatientChatHistory(String email) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // entity -> dto
        return chatLogRepository.findAllByPatientOrderByDisplayOrderAsc(patient)
                .stream()
                .map(chatMapper::toResponse)
                .toList();
    }

    // 의사 채팅 기록 조회
    public List<ChatResponse> getDoctorChatHistory(String licenseId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // entity -> dto
        return chatLogRepository.findAllByDoctorOrderByDisplayOrderAsc(doctor)
                .stream()
                .map(chatMapper::toResponse)
                .toList();
    }
}

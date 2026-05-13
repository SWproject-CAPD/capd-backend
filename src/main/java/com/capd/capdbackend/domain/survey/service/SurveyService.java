package com.capd.capdbackend.domain.survey.service;

import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdStatus;
import com.capd.capdbackend.domain.capd.repository.CapdCommonRepository;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.exception.DoctorErrorCode;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.report.client.GeminiApiClient;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import com.capd.capdbackend.domain.reservation.exception.ReservationErrorCode;
import com.capd.capdbackend.domain.reservation.repository.ReservationRepository;
import com.capd.capdbackend.domain.survey.dto.request.AnswerListRequest;
import com.capd.capdbackend.domain.survey.dto.request.AnswerRequest;
import com.capd.capdbackend.domain.survey.dto.response.AnswerResponse;
import com.capd.capdbackend.domain.survey.dto.response.PatientQuestionResponse;
import com.capd.capdbackend.domain.survey.dto.response.QuestionResponse;
import com.capd.capdbackend.domain.survey.entity.AnswerResultEntity;
import com.capd.capdbackend.domain.survey.entity.QuestionRecommendEntity;
import com.capd.capdbackend.domain.survey.entity.QuestionStatus;
import com.capd.capdbackend.domain.survey.entity.QuestionType;
import com.capd.capdbackend.domain.survey.exception.SurveyErrorCode;
import com.capd.capdbackend.domain.survey.mapper.AnswerMapper;
import com.capd.capdbackend.domain.survey.mapper.QuestionMapper;
import com.capd.capdbackend.domain.survey.repository.AnswerResultRepository;
import com.capd.capdbackend.domain.survey.repository.QuestionRecommendRepository;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SurveyService {

    private final QuestionRecommendRepository questionRecommendRepository;
    private final AnswerResultRepository answerResultRepository;
    private final CapdCommonRepository capdCommonRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReservationRepository reservationRepository;
    private final GeminiApiClient geminiApiClient;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final ObjectMapper objectMapper;

    // 질문 생성
    @Transactional
    public QuestionResponse createQuestion(String licenseId, Long reservationId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 예약이 존재하는지 조회
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 담당 환자 예약인지 확인
        if (!reservation.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        PatientEntity patient = reservation.getPatient();

        // 최근 7일치 투석 데이터 조회
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        List<CapdCommonEntity> records = capdCommonRepository.findAllByPatientAndStatusAndDateBetweenOrderByDateAsc(patient, CapdStatus.SUBMITTED, startDate, endDate);

        // Gemini 프롬프트 구성
        String prompt = buildQuestionPrompt(patient.getUser().getUserName(), records);

        // Gemini API 호출
        String geminiResponse = geminiApiClient.generateContent(prompt);

        // Gemini 응답 파싱
        Map<String, Object> parsed = parseGeminiResponse(geminiResponse);
        String questionText = (String) parsed.get("questionText");
        String typeStr = (String) parsed.get("questionType");

        String options = null;
        if (parsed.containsKey("options") && parsed.get("options") != null) {
            try {
                // List 형태를 JSON 문자열로 변환
                options = objectMapper.writeValueAsString(parsed.get("options"));
            }
            catch (Exception e) {
                log.error("options 파싱 실패: {}", e.getMessage());
            }
        }

        // questionReason 값 추출
        String questionReason = (String) parsed.get("questionReason");

        // 질문이 서술형이면 enum 클래스로 바꿈
        QuestionType type = QuestionType.valueOf(typeStr);

        // 질문 저장
        QuestionRecommendEntity question = QuestionRecommendEntity.builder()
                .doctor(doctor)
                .patient(patient)
                .reservation(reservation)
                .question(questionText)
                .type(type)
                .options(options)
                .questionReason(questionReason)
                .status(QuestionStatus.PENDING)
                .build();

        // db에 저장
        questionRecommendRepository.save(question);

        // 로그 출력
        log.info("질문 생성 완료: reservationId={}, type={}", reservationId, type);

        // entity -> dto
        return questionMapper.toQuestionResponse(question);
    }

    // Gemini 프롬프트 구성
    private String buildQuestionPrompt(String patientName, List<CapdCommonEntity> records) {

        StringBuilder dataBuilder = new StringBuilder();
        for (CapdCommonEntity record : records) {
            dataBuilder.append(String.format(
                    "날짜: %s, 체중: %.1fkg, 혈압: %d/%d, 혈당: %.0fmg/dL, 총초여과량: %.0fg\n",
                    record.getDate(),
                    record.getBodyWeight(),
                    record.getBloodPressureSys(),
                    record.getBloodPressureDia(),
                    record.getFastingBloodSugar(),
                    record.getTotalUltrafiltration()
            ));
        }

        return String.format("""
                당신은 CAPD(복막투석) 전문 의료 AI 어시스턴트입니다.
                아래는 %s 환자의 최근 투석 데이터입니다.
                                
                %s
                                
                위 데이터를 바탕으로 진료 전 환자에게 물어볼 질문 1개를 생성해주세요.
                질문 유형은 MULTIPLE_CHOICE, YES_NO, DESCRIPTIVE 중 하나를 선택하세요.
                                
                반드시 아래 JSON 형식으로만 응답해주세요. 다른 텍스트는 포함하지 마세요.
                                
                객관식 예시:
                {
                  "questionType": "MULTIPLE_CHOICE",
                  "questionText": "어제 저녁 식사로 주로 어떤 음식을 드셨나요?",
                  "options": ["국물류", "튀김류", "육류", "채소류", "기타"],
                  "questionReason": "최근 체중 증가 추세로 식단 관련 질문 추천"
                }
                                
                예/아니요 예시:
                {
                  "questionType": "YES_NO",
                  "questionText": "어제 처방받은 혈압약을 제시간에 복용하셨나요?",
                  "options": ["예", "아니요"],
                  "questionReason": "혈압 수치가 높은 편으로 약 복용 여부 확인 필요"
                }
                                
                서술형 예시:
                {
                  "questionType": "DESCRIPTIVE",
                  "questionText": "최근 투석액을 배액할 때 불편함이 있었다면 어떤 느낌인지 적어주세요.",
                  "options": null,
                  "questionReason": "배액 혼탁 발생으로 증상 파악 필요"
                }
                """,
                patientName, dataBuilder.toString()
        );
    }

    // Gemini 응답 JSON 파싱
    private Map<String, Object> parseGeminiResponse(String response) {
        try {
            // 코드 블록 제거 (```json ... ```)
            String cleaned = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            return objectMapper.readValue(cleaned, Map.class);
        }
        catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("Gemini 응답 파싱 실패");
        }
    }

    // 질문 승인
    @Transactional
    public QuestionResponse approveQuestion(String licenseId, Long questionId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 질문이 있는지 조회
        QuestionRecommendEntity question = questionRecommendRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.QUESTION_NOT_FOUND));

        // 의사의 본인의 질문인지 확인
        if (!question.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(SurveyErrorCode.QUESTION_NO_PERMISSION);
        }

        // 승인
        question.approve();

        // 로그 출력
        log.info("질문 승인 완료: questionId={}", questionId);

        // entity -> dto
        return questionMapper.toQuestionResponse(question);
    }

    // 질문 거절
    @Transactional
    public QuestionResponse rejectQuestion(String licenseId, Long questionId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 질문이 있는지 조회
        QuestionRecommendEntity question = questionRecommendRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.QUESTION_NOT_FOUND));

        // 본인 질문인지 확인
        if (!question.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(SurveyErrorCode.QUESTION_NO_PERMISSION);
        }

        // 거절
        question.reject();

        // 로그 출력
        log.info("질문 거절 완료: questionId={}", questionId);

        // entity -> dto
        return questionMapper.toQuestionResponse(question);
    }

    // 환자가 의사가 승인한 질문 조회
    public List<PatientQuestionResponse> checkQuestion(String email, Long reservationId) {

        // 환자 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 예약이 존재하는지 조회
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 본인 예약인지 확인
        if (!reservation.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_NO_PERMISSION);
        }

        // 예약 전날까지만 조회 가능
        LocalDate deadline = reservation.getReservationDate().toLocalDate().minusDays(1);
        if (LocalDate.now().isAfter(deadline)) {
            throw new CustomException(SurveyErrorCode.ANSWER_DEADLINE_PASSED);
        }

        // entity -> dto
        return questionRecommendRepository.findAllByReservationAndStatusOrderByCreatedAtDesc(reservation, QuestionStatus.APPROVED).stream()
                .map(questionMapper::toPatientQuestionResponse)
                .toList();
    }

    // 환자가 승인된 질문에 대한 답변
    @Transactional
    public List<AnswerResponse> answerQuestion(String email, Long reservationId, AnswerListRequest request) {

        // 환자 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 예약 조회
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 본인 예약인지 확인
        if (!reservation.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_NO_PERMISSION);
        }

        // 예약 전날까지만 답변 가능
        LocalDate deadline = reservation.getReservationDate()
                .toLocalDate().minusDays(1);
        if (LocalDate.now().isAfter(deadline)) {
            throw new CustomException(SurveyErrorCode.ANSWER_DEADLINE_PASSED);
        }


        // 각 답변 저장
        List<AnswerResponse> resultList = new ArrayList<>();

        for (AnswerRequest answerRequest : request.getAnswers()) {

            // 질문 조회
            QuestionRecommendEntity question = questionRecommendRepository
                    .findByQuestionId(answerRequest.getQuestionId())
                    .orElseThrow(() -> new CustomException(SurveyErrorCode.QUESTION_NOT_FOUND));

            // 승인된 질문인지 확인
            if (question.getStatus() != QuestionStatus.APPROVED) {
                throw new CustomException(SurveyErrorCode.QUESTION_NOT_APPROVED);
            }

            // 이미 답변했는지 확인
            if (answerResultRepository.existsByQuestionAndPatient(question, patient)) {
                throw new CustomException(SurveyErrorCode.QUESTION_ALREADY_ANSWERED);
            }

            // 답변 저장
            AnswerResultEntity answer = AnswerResultEntity.builder()
                    .question(question)
                    .patient(patient)
                    .answer(answerRequest.getAnswer())
                    .build();

            answerResultRepository.save(answer);
            resultList.add(answerMapper.toResponse(answer));
        }

        log.info("전체 답변 제출 완료: reservationId={}, patientId={}", reservationId, patient.getPatientId());

        return resultList;
    }

    // 의사가 특정 예약에 대한 질문들 조회
    public List<QuestionResponse> checkReservationQuestion(String licenseId, Long reservationId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 예약이 존재하는지 조회
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 담당 예약인지 확인
        if (!reservation.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        // entity -> dto
        return questionRecommendRepository
                .findAllByReservationOrderByCreatedAtDesc(reservation)
                .stream()
                .map(questionMapper::toQuestionResponse)
                .toList();
    }

    // 의사가 환자가 작성한 질문을 조회 (특정 예약을 기준)
    public List<AnswerResponse> checkAnswer(String licenseId, Long reservationId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 예약 조회
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        // 담당 예약인지 확인
        if (!reservation.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        // 해당 예약의 질문들에 달린 답변 조회
        List<QuestionRecommendEntity> questions = questionRecommendRepository.findAllByReservationOrderByCreatedAtDesc(reservation);

        // entity -> dto
        return questions.stream()
                .flatMap(q -> answerResultRepository
                        .findByQuestionAndPatient(q, reservation.getPatient())
                        .stream())
                .map(answerMapper::toResponse)
                .toList();
    }

    // 승인, 거절한 질문 되돌리기
    @Transactional
    public QuestionResponse resetQuestion(String licenseId, Long questionId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 질문 조회
        QuestionRecommendEntity question = questionRecommendRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.QUESTION_NOT_FOUND));

        // 본인 질문인지 확인
        if (!question.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(SurveyErrorCode.QUESTION_NO_PERMISSION);
        }

        // 환자가 답변했는지 확인
        if (answerResultRepository.existsByQuestionAndPatient(question, question.getPatient())) {
            throw new CustomException(SurveyErrorCode.QUESTION_ALREADY_ANSWERED);
        }

        // 되돌리기
        question.reset();

        // 로그 출력
        log.info("질문 상태 되돌리기 완료: questionId={}", questionId);

        // entity -> dto
        return questionMapper.toQuestionResponse(question);
    }
}

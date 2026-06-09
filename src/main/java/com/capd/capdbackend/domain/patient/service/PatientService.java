package com.capd.capdbackend.domain.patient.service;

import com.capd.capdbackend.domain.anomaly.repository.AnomalyResultRepository;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.repository.CapdCommonRepository;
import com.capd.capdbackend.domain.capd.repository.CapdSessionRepository;
import com.capd.capdbackend.domain.chat.repository.ChatLogRepository;
import com.capd.capdbackend.domain.patient.dto.request.PatientSignUpRequest;
import com.capd.capdbackend.domain.patient.dto.response.PatientInfoResponse;
import com.capd.capdbackend.domain.patient.dto.response.PatientSignUpResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.mapper.PatientInfoMapper;
import com.capd.capdbackend.domain.patient.mapper.PatientSignUpMapper;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.report.repository.ReportRepository;
import com.capd.capdbackend.domain.reservation.entity.ReservationEntity;
import com.capd.capdbackend.domain.reservation.repository.ReservationRepository;
import com.capd.capdbackend.domain.survey.entity.QuestionRecommendEntity;
import com.capd.capdbackend.domain.survey.repository.AnswerResultRepository;
import com.capd.capdbackend.domain.survey.repository.QuestionRecommendRepository;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.domain.user.repository.UserRepository;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PatientService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PatientSignUpMapper patientSignUpMapper;
    private final PasswordEncoder passwordEncoder;
    private final PatientInfoMapper patientInfoMapper;
    private final ReservationRepository reservationRepository;
    private final QuestionRecommendRepository questionRecommendRepository;
    private final AnswerResultRepository answerResultRepository;
    private final ChatLogRepository chatLogRepository;
    private final AnomalyResultRepository anomalyResultRepository;
    private final ReportRepository reportRepository;
    private final CapdCommonRepository capdCommonRepository;
    private final CapdSessionRepository capdSessionRepository;

    // 환자 회원가입
    @Transactional
    public PatientSignUpResponse patientSignUp(PatientSignUpRequest request) {

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 전화번호 중복 검사
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new CustomException(UserErrorCode.DUPLICATE_PHONE);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // dto -> entity (UserEntity)
        UserEntity userEntity = patientSignUpMapper.toUserEntity(request, encodedPassword);

        // db에 저장
        UserEntity savedUser = userRepository.save(userEntity);

        // dto -> entity (PatientEntity)
        PatientEntity patientEntity = patientSignUpMapper.toPatientEntity(request, savedUser);

        // db에 저장
        PatientEntity savedPatient = patientRepository.save(patientEntity);

        // 로그 출력
        log.info("환자 회원가입 성공: name={}", savedPatient.getUser().getUserName());

        // entity -> response DTO
        return patientSignUpMapper.toResponse(savedPatient, savedUser);
    }

    // 환자 본인 회원 정보 조회
    public PatientInfoResponse patientInfo(String email) {

        // 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // UserEntity 꺼내기
        UserEntity user = patient.getUser();

        // entity -> response dto
        return patientInfoMapper.toResponse(patient, user);
    }

    // 환자 회원탈퇴
    @Transactional
    public void patientDelete(String email) {

        // 유저 조회
        PatientEntity patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // UserEntity 꺼내두기 => patient에서 삭제되면 user에서도 삭제
        UserEntity user = patient.getUser();

        // 환자 채팅 기록 삭제
        chatLogRepository.deleteAllByPatient(patient);

        // 이상치 분석 결과 삭제
        anomalyResultRepository.deleteAllByPatient(patient);

        // 보고서 삭제
        reportRepository.deleteAllByPatient(patient);

        // 투석 일지 세션 삭제 후 공통 삭제
        List<CapdCommonEntity> capdCommons = capdCommonRepository.findAllByPatientOrderByDateDesc(patient);
        for (CapdCommonEntity capdCommon : capdCommons) {
            capdSessionRepository.deleteAllByCapdCommon(capdCommon);
        }
        capdCommonRepository.deleteAllByPatient(patient);

        // 연관된 예약 삭제 (예약 → 설문 질문 → 답변 순서로)
        List<ReservationEntity> reservations = reservationRepository.findAllByPatient(patient);
        for (ReservationEntity reservation : reservations) {
            List<QuestionRecommendEntity> questions = questionRecommendRepository.findAllByReservation(reservation);
            answerResultRepository.deleteAllByQuestionIn(questions);
            questionRecommendRepository.deleteAllByReservation(reservation);
        }
        reservationRepository.deleteAllByPatient(patient);

        // 환자 사용자 삭제
        patientRepository.delete(patient);

        // user 테이블에서도 삭제
        userRepository.delete(user);

        // 삭제 성공하면 로그 출력
        log.info("환자 사용자 삭제 성공");
    }
}

package com.capd.capdbackend.domain.user.service;

import com.capd.capdbackend.domain.user.dto.request.PatientSignUpRequest;
import com.capd.capdbackend.domain.user.dto.response.SignUpResponse;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.domain.user.mapper.PatientSignUpMapper;
import com.capd.capdbackend.domain.user.repository.UserRepository;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PatientService {

    private final UserRepository userRepository;
    private final PatientSignUpMapper patientSignUpMapper;
    private final PasswordEncoder passwordEncoder;

    // 환자 회원가입
    @Transactional
    public SignUpResponse signUp(PatientSignUpRequest request) {

        // 중복 이메일 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 중복 전화번호 검사
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new CustomException(UserErrorCode.DUPLICATE_PHONE);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // dto -> entity
        UserEntity userEntity = patientSignUpMapper.toEntity(request, encodedPassword);

        // db 저장
        UserEntity savedUser = userRepository.save(userEntity);

        // 로그 출력
        log.info("회원가입 성공: name={}", savedUser.getName());

        // entity -> response dto
        return patientSignUpMapper.toResponse(savedUser);
    }

    // 환자인 사용자 삭제
    @Transactional
    public void deletePatient(String email) {

        // 환자가 DB에 존재하는지 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 삭제
        userRepository.delete(user);

        // 삭제 성공 시 로그 출력
        log.info("환자 삭제 성공: email={}", email);
    }
}

package com.capd.capdbackend.domain.user.service;

import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.user.dto.request.PasswordChangeRequest;
import com.capd.capdbackend.domain.user.dto.request.PasswordResetRequest;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.domain.user.repository.UserRepository;
import com.capd.capdbackend.global.exception.CustomException;
import com.capd.capdbackend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSendService emailSendService;

    // 임시 비밀번호 생성
    private String createTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 임시 비밀번호 발급
    @Transactional
    public void resetPassword(PasswordResetRequest request) {

        // 이메일로 유저 조회
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 임시 비밀번호 생성
        String tempPassword = createTempPassword();

        // 임시 비밀번호 암호화 뒤 DB에 저장
        user.changePassword(passwordEncoder.encode(tempPassword));

        // 이메일 발송
        emailSendService.sendTempPassword(request.getEmail(), tempPassword);

        // 로그 출력
        log.info("임시 비밀번호 발급 완료: {}", request.getEmail());
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String identifier, PasswordChangeRequest request) {

        // 이메일로 환자 조회 or 면허번호로 의사 조회
        UserEntity user = userRepository.findByEmail(identifier)
                .orElseGet(() -> doctorRepository.findByLicenseId(identifier)
                        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
                        .getUser());

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(UserErrorCode.PASSWORD_NOT_MATCH);
        }

        // 새 비밀번호 암호화 후 DB에 저장
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));

        // 로그 출력
        log.info("비밀번호 변경 완료: {}", identifier);
    }
}

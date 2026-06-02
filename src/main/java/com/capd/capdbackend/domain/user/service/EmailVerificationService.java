package com.capd.capdbackend.domain.user.service;

import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailSendService emailSendService;
    private static final long VERIFICATION_CODE_TTL = 5; // 5분으로 설정

    // 6자리 랜덤 숫자 생성
    private String createCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    // 인증 코드 발송
    public void sendVerificationCode(String email) {

        // 6자리 숫자 자동 생성
        String code = createCode();

        // Redis에 저장
        redisTemplate.opsForValue().set(
                "email:verification:" + email, // 키
                code, // 인증 코드
                VERIFICATION_CODE_TTL, // 만료시간 (5분)
                TimeUnit.MINUTES // 만료 시간 단위(분)
        );

        // 이메일 발송
        emailSendService.sendVerificationCode(email, code);

        // 로그 출력
        log.info("[EmailVerificationService] 이메일 인증 코드 발송: {}", email);
    }

    // 인증 코드 확인
    public boolean verifyCode(String email, String code) {

        String savedCode = redisTemplate.opsForValue().get("email:verification:" + email);

        // 코드가 만료되거나 없을 때
        if (savedCode == null) {
            throw new CustomException(UserErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        // 코드 불일치
        if (!savedCode.equals(code)) {
            throw new CustomException(UserErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 인증 성공하면 Redis에서 코드 삭제
        redisTemplate.delete("email:verification:" + email);

        // 인증 완료 표시 저장 (10분 유효)
        redisTemplate.opsForValue().set(
                "email:verified:" + email, // 키
                "true", // 값(인증 완료 표시)
                10, // 만료 시간
                TimeUnit.MINUTES // 만료 시간 단위
        );

        // 로그 출력
        log.info("[EmailVerificationService] 이메일 인증 완료: {}", email);

        return true;
    }

    // 이메일 인증 여부 확인
    public boolean isVerified(String email) {
        return Boolean.TRUE.toString().equals(
                redisTemplate.opsForValue().get("email:verified:" + email)
        );
    }
}

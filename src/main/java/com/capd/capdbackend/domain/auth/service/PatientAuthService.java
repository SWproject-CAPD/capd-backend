package com.capd.capdbackend.domain.auth.service;

import com.capd.capdbackend.domain.auth.dto.request.PatientLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.PatientLoginResponse;
import com.capd.capdbackend.domain.auth.exception.AuthErrorCode;
import com.capd.capdbackend.domain.auth.mapper.PatientAuthMapper;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.exception.PatientErrorCode;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.domain.user.repository.UserRepository;
import com.capd.capdbackend.global.exception.CustomException;
import com.capd.capdbackend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PatientAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final PatientAuthMapper patientAuthMapper;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    // 로그인
    @Transactional
    public PatientLoginResponse patientLogin(PatientLoginRequest request) {

        // 이메일과 비밀번호로 인증 시도
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        try {
            // CustomUserDetailsService 호출 -> 비밀번호 검증
            authenticationManager.authenticate(authenticationToken);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
        }

        // 인증 통과 후 이메일로 공통 UserEntity 찾기
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // UserEntity를 기반으로 PatientEntity 찾기
        PatientEntity patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(PatientErrorCode.PATIENT_NOT_FOUND));

        // accessToken 및 refreshToken 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().toString(), "custom");
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());

        // refreshToken을 DB에 저장
        user.createRefreshToken(refreshToken);

        // accessToken의 만료 시간 가져오기
        Long expirationTime = jwtProvider.getExpiration(accessToken);

        // 로그인 성공하면 로그 출력
        log.info("환자 로그인 성공: 이메일 {}", user.getEmail());

        // 응답 반환
        return patientAuthMapper.toResponse(user, patient, accessToken, expirationTime);
    }

    @Transactional
    public void patientLogout(String token) {

        // 토큰에서 Bearer 제거 후 이메일 추출
        String resolvedToken = token.substring(7);
        String email = jwtProvider.extractSocialId(resolvedToken);

        // DB에서 유저 찾기
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 리프레시 토큰 삭제 => Null로 업데이트 함
        user.expireRefreshToken();

        log.info("로그아웃 성공: {}", email);
    }
}

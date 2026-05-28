package com.capd.capdbackend.domain.auth.service;

import com.capd.capdbackend.domain.auth.dto.request.PatientLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.PatientLoginResponse;
import com.capd.capdbackend.domain.auth.dto.response.RefreshTokenResponse;
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
            // CustomUserDetailsService 호출 → 비밀번호 검증
            authenticationManager.authenticate(authenticationToken);
        }
        catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
        }

        // 이메일로 유저 조회
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 유저로 환자 조회
        PatientEntity patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(PatientErrorCode.PATIENT_NOT_FOUND));

        // Access Token 및 Refresh Token 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().toString(), "custom");
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());

        // Refresh Token DB에 저장
        user.createRefreshToken(refreshToken);

        // Access Token 만료 시간 가져오기
        Long expirationTime = jwtProvider.getExpiration(accessToken);

        // 로그
        log.info("환자 로그인 성공: 이메일 {}", user.getEmail());

        // 응답 반환
        return patientAuthMapper.toResponse(user, patient, accessToken, expirationTime);
    }

    // 로그아웃
    @Transactional
    public void patientLogout(String token) {

        // 토큰에서 Bearer 제거 후 이메일 추출
        String resolvedToken = token.substring(7);
        String email = jwtProvider.extractSocialId(resolvedToken);

        // DB에서 유저 찾기
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // Refresh Token 삭제
        user.expireRefreshToken();

        log.info("로그아웃 성공: {}", email);
    }

    // 환자 토큰 재발급
    @Transactional
    public RefreshTokenResponse refreshToken(String refreshToken) {

        // Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh Token에서 이메일 추출
        String email = jwtProvider.extractSocialId(refreshToken);

        // 이메일로 유저 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // DB에 저장된 Refresh Token과 비교
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새 Access Token 발급 (환자는 이메일로 생성)
        String newAccessToken = jwtProvider.createAccessToken(
                email,
                user.getRole().toString(),
                "custom"
        );

        // 새 Refresh Token 발급
        String newRefreshToken = jwtProvider.createRefreshToken(
                email,
                UUID.randomUUID().toString()
        );

        // 새 Refresh Token DB에 저장
        user.createRefreshToken(newRefreshToken);

        // 새 Access Token 만료 시간 가져오기
        Long expirationTime = jwtProvider.getExpiration(newAccessToken);

        log.info("환자 토큰 재발급 성공: 이메일 {}", email);

        // 응답 반환 (새 Refresh Token은 Controller에서 Cookie로 반환)
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expirationTime(expirationTime)
                .build();
    }
}
package com.capd.capdbackend.domain.auth.service;

import com.capd.capdbackend.domain.auth.dto.request.DoctorLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.DoctorLoginResponse;
import com.capd.capdbackend.domain.auth.dto.response.RefreshTokenResponse;
import com.capd.capdbackend.domain.auth.exception.AuthErrorCode;
import com.capd.capdbackend.domain.auth.mapper.DoctorAuthMapper;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
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
public class DoctorAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final DoctorAuthMapper doctorAuthMapper;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    // 로그인
    @Transactional
    public DoctorLoginResponse doctorLogin(DoctorLoginRequest request) {

        // 면허번호와 비밀번호로 인증 시도
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getLicenseId(), request.getPassword());

        try {
            // CustomUserDetailsService의 loadUserByUsername() 호출
            authenticationManager.authenticate(authenticationToken);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
        }

        // 유저 확인
        DoctorEntity doctor = doctorRepository.findByLicenseId(request.getLicenseId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        UserEntity user = doctor.getUser();

        // Access Token 및 Refresh Token 발급
        String accessToken = jwtProvider.createAccessToken(request.getLicenseId(), user.getRole().toString(), "custom");
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());

        // Refresh Token DB에 저장
        user.createRefreshToken(refreshToken);

        // Access Token 만료 시간 가져오기
        Long expirationTime = jwtProvider.getExpiration(accessToken);

        // 로그인 성공 로그
        log.info("의사 로그인 성공: 면허번호 {}, 이메일 {}", request.getLicenseId(), user.getEmail());

        // 응답 반환 (Access Token은 Body로)
        return doctorAuthMapper.toResponse(user, doctor, accessToken, expirationTime);
    }

    // 로그아웃
    @Transactional
    public void doctorLogout(String token) {

        // 토큰에서 Bearer 제거 후 면허번호 추출
        String resolvedToken = token.substring(7);
        String licenseId = jwtProvider.extractSocialId(resolvedToken);

        // DB에서 의사 찾기
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        UserEntity user = doctor.getUser();

        // Refresh Token 삭제
        user.expireRefreshToken();

        log.info("로그아웃 성공: {}", licenseId);
    }

    // 의사 토큰 재발급
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

        // 유저로 의사 조회
        DoctorEntity doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // DB에 저장된 Refresh Token과 비교
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새 Access Token 발급 (의사는 면허번호로 생성)
        String newAccessToken = jwtProvider.createAccessToken(
                doctor.getLicenseId(),
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

        log.info("의사 토큰 재발급 성공: 이메일 {}", email);

        // 응답 반환 (새 Refresh Token은 Controller에서 Cookie로 반환)
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expirationTime(expirationTime)
                .build();
    }
}
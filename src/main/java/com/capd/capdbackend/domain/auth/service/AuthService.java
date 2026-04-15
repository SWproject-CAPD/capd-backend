package com.capd.capdbackend.domain.auth.service;

import com.capd.capdbackend.domain.auth.dto.request.DoctorLoginRequest;
import com.capd.capdbackend.domain.auth.dto.request.PatientLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.LoginResponse;
import com.capd.capdbackend.domain.auth.exception.AuthErrorCode;
import com.capd.capdbackend.domain.auth.mapper.AuthMapper;
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
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final AuthMapper authMapper;

    // 의사 로그인
    @Transactional
    public LoginResponse doctorLogin(DoctorLoginRequest request) {

        // 유저 확인
        UserEntity user = userRepository.findByLicenseId(request.getLicenseId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 인증 토큰 완성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getLicenseId(), request.getPassword());

        try {
            // 인증 처리 -> 비밀번호 검증
            authenticationManager.authenticate(authenticationToken);
        }
        catch (org.springframework.security.authentication.BadCredentialsException e) {
            // 비밀번호가 틀렸을때 에러 메시지 출력
            throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
        }

        // accessToken 및 refreshToken 발급
        String accessToken = jwtProvider.createAccessToken(user.getLicenseId(), user.getRole().toString(), "custom");
        String refreshToken = jwtProvider.createRefreshToken(user.getLicenseId(), UUID.randomUUID().toString());

        // refreshToken을 db에 저장
        user.createRefreshToken(refreshToken);

        // accessToken의 만료 시간 가져오기
        Long expirationTime = jwtProvider.getExpiration(accessToken);

        // 로그인 성공하면 로그 남기기
        log.info("로그인 성공: {}", user.getName());

        // 응답 반환
        return authMapper.toResponse(user, accessToken, expirationTime);
    }

    // 환자 로그인
    @Transactional
    public LoginResponse patientLogin(PatientLoginRequest request) {

        // 유저 확인
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 인증 토큰 완성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        try {
            // 인증 처리 -> 비밀번호 검증
            authenticationManager.authenticate(authenticationToken);
        }
        catch (org.springframework.security.authentication.BadCredentialsException e) {
            // 비밀번호가 틀렸을때 에러 메시지 출력
            throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
        }

        // accessToken 및 refreshToken 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().toString(), "custom");
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());

        // refreshToken을 db에 저장
        user.createRefreshToken(refreshToken);

        // accessToken의 만료 시간 가져오기
        Long expirationTime = jwtProvider.getExpiration(accessToken);

        // 로그인 성공하면 로그 남기기
        log.info("로그인 성공: {}", user.getEmail());

        // 응답 반환
        return authMapper.toResponse(user, accessToken, expirationTime);

    }

    // 의사 로그아웃 로직
    @Transactional
    public void doctorLogout(String token) {

        // 토큰에서 Bearer 제거 후 이메일 추출
        String resolvedToken = token.substring(7);
        String licenseId = jwtProvider.extractSocialId(resolvedToken);

        // DB에서 유저 찾기
        UserEntity user = userRepository.findByLicenseId(licenseId).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 리프레시 토큰 삭제 => Null로 업데이트 함
        user.expireRefreshToken();

        log.info("로그아웃 성공: {}", licenseId);
    }

    // 환자 로그아웃 로직
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

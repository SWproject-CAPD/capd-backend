package com.capd.capdbackend.domain.auth.service;

import com.capd.capdbackend.domain.auth.dto.request.DoctorLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.DoctorLoginResponse;
import com.capd.capdbackend.domain.auth.exception.AuthErrorCode;
import com.capd.capdbackend.domain.auth.mapper.DoctorAuthMapper;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.exception.DoctorErrorCode;
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

        // accessToken 및 refreshToken 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().toString(), "custom");
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());

        // refreshToken을 DB에 저장
        user.createRefreshToken(refreshToken);

        // accessToken의 만료 시간 가져오기
        Long expirationTime = jwtProvider.getExpiration(accessToken);

        // 로그인 성공하면 로그 남기기
        log.info("의사 로그인 성공: 면허번호 {}, 이메일 {}", request.getLicenseId(), user.getEmail());

        // 응답 반환
        return doctorAuthMapper.toResponse(user, doctor, accessToken, expirationTime);
    }
}

package com.capd.capdbackend.domain.auth.service;

import com.capd.capdbackend.domain.auth.dto.request.DoctorLoginRequest;
import com.capd.capdbackend.domain.auth.dto.response.DoctorLoginResponse;
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

        // accessToken 및 refreshToken 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().toString(), "custom"); // 토큰 안에 공통 정보인 이메일을 넣음
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

    // 로그아웃
    @Transactional
    public void doctorLogout(String token) {

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

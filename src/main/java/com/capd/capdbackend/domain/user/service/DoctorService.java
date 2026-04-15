package com.capd.capdbackend.domain.user.service;

import com.capd.capdbackend.domain.user.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.user.dto.response.SignUpResponse;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.domain.user.mapper.DoctorSignUpMapper;
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
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorSignUpMapper doctorSignUpMapper;
    private final PasswordEncoder passwordEncoder;

    // 의사 회원가입
    @Transactional
    public SignUpResponse signUp(DoctorSignUpRequest request) {

        // 의사 면허번호 중복 검사
        if (userRepository.existsByLicenseId(request.getLicenseId())) {
            throw new CustomException(UserErrorCode.DUPLICATE_LICENSEID);
        }

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
        UserEntity userEntity = doctorSignUpMapper.toEntity(request, encodedPassword);

        // db 저장
        UserEntity savedUser = userRepository.save(userEntity);

        // 로그 출력
        log.info("회원가입 성공 : name={}", savedUser.getName());

        // entity -> response dto
        return doctorSignUpMapper.toResponse(savedUser);
    }

    // 의사인 사용자 삭제
    @Transactional
    public void deleteDoctor(String licenseId) {

        // 유저 조회
        UserEntity user = userRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 유저 삭제
        userRepository.delete(user);

        // 삭제 성공 시 로그 출력
        log.info("의사 사용자 삭제 성공: licenseId={}", licenseId);
    }
}

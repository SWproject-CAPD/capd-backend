package com.capd.capdbackend.domain.doctor.service;

import com.capd.capdbackend.domain.doctor.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.doctor.dto.response.DoctorInfoResponse;
import com.capd.capdbackend.domain.doctor.dto.response.DoctorSignUpResponse;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.exception.DoctorErrorCode;
import com.capd.capdbackend.domain.doctor.mapper.DoctorInfoMapper;
import com.capd.capdbackend.domain.doctor.mapper.DoctorSignUpMapper;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
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
    private final DoctorRepository doctorRepository;
    private final DoctorSignUpMapper doctorSignUpMapper;
    private final PasswordEncoder passwordEncoder;
    private final DoctorInfoMapper doctorInfoMapper;

    // 의사 회원가입
    @Transactional
    public DoctorSignUpResponse doctorSignUp(DoctorSignUpRequest request) {

        // 의사 면허번호 중복 검사
        if (doctorRepository.existsByLicenseId(request.getLicenseId()))
            throw new CustomException(DoctorErrorCode.LICENSE_ID_DUPLICATE);

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail()))
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);

        // 전화번호 중복 검사
        if (userRepository.existsByPhone(request.getPhone()))
            throw new CustomException(UserErrorCode.DUPLICATE_PHONE);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // dto -> entity (UserEntity 먼저)
        UserEntity userEntity = doctorSignUpMapper.toUserEntity(request, encodedPassword);

        // db에 저장
        UserEntity savedUser = userRepository.save(userEntity);

        // dto -> entity (DoctorEntity는 나중에)
        DoctorEntity doctorEntity = doctorSignUpMapper.toDoctorEntity(request, savedUser);

        // db에 저장
        DoctorEntity savedDoctor = doctorRepository.save(doctorEntity);

        // 로그 출력
        log.info("의사 회원가입 성공: name={}", savedDoctor.getUser().getUserName());

        // entity -> response DTO
        return doctorSignUpMapper.toResponse(savedDoctor, savedUser);
    }

    // 의사 본인 정보 조회 로직
    public DoctorInfoResponse doctorInfo(String licenseId) {

        // 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // UserEntity 꺼내기
        UserEntity user = doctor.getUser();

        // 응답 반환
        return doctorInfoMapper.toResponse(doctor, user);
    }

    // 의사 회원탈퇴
    @Transactional
    public void doctorDelete(String licenseId) {

        // 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 의사 사용자 삭제
        doctorRepository.delete(doctor);

        // 삭제 성공하면 로그 출력
        log.info("의사 사용자 삭제 성공");
    }
}

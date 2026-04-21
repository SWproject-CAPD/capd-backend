package com.capd.capdbackend.domain.doctor.service;

import com.capd.capdbackend.domain.doctor.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.doctor.dto.request.PatientRegisterRequest;
import com.capd.capdbackend.domain.doctor.dto.response.*;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.exception.DoctorErrorCode;
import com.capd.capdbackend.domain.doctor.mapper.*;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.patient.exception.PatientErrorCode;
import com.capd.capdbackend.domain.patient.repository.PatientRepository;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.exception.UserErrorCode;
import com.capd.capdbackend.domain.user.repository.UserRepository;
import com.capd.capdbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

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
    private final PatientRepository patientRepository;
    private final PatientRegisterMapper patientRegisterMapper;
    private final PatientAllSearchMapper patientAllSearchMapper;
    private final PatientProfileMapper patientProfileMapper;

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

        // entity -> response DTO
        return doctorInfoMapper.toResponse(doctor, user);
    }

    // 의사 회원탈퇴
    @Transactional
    public void doctorDelete(String licenseId) {

        // 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // UserEntity 꺼내두기 => doctor에서 삭제되면 user에서도 삭제
        UserEntity user = doctor.getUser();

        // 의사 사용자 삭제
        doctorRepository.delete(doctor);

        // user 테이블에서도 삭제
        userRepository.delete(user);

        // 삭제 성공하면 로그 출력
        log.info("의사 사용자 삭제 성공");
    }

    // 가입한 환자를 본인의 환자로 등록
    @Transactional
    public PatientRegisterResponse patientRegister(String licenseId, PatientRegisterRequest request) {

        // 의사 정보 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(DoctorErrorCode.LICENSE_ID_NOT_FOUND));

        // 전화번호로 사용자 조회
        UserEntity user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 사용자 조회로 찾은 환자 정보 조회
        PatientEntity patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 이미 등록된 환자인지 확인
        if (patient.getDoctor() != null) {
            throw new CustomException(DoctorErrorCode.PATIENT_ALREADY_REGISTERED);
        }

        // 환자 객체에 의사를 등록
        patient.assignDoctor(doctor);

        // 로그 출력
        log.info("환자 등록 성공: patientId={}", patient.getPatientId());

        // entity -> response DTO
        return patientRegisterMapper.toResponse(patient, user);
    }

    // 환자 목록 전체 보기
    public List<PatientAllSearchResponse> patientAll(String licenseId) {

        // 의사 유저 조회
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자 목록 전체 조회
        List<PatientEntity> patients = patientRepository.findByDoctor(doctor);

        // 변환된 dto 담을 빈 상자
        List<PatientAllSearchResponse> list = new ArrayList<>();

        // for문 돌면서 상자에 하나씩 담기
        for (PatientEntity patient : patients) {

            // entity -> dto 변환
            PatientAllSearchResponse patientAllSearchResponse = patientAllSearchMapper.toResponse(patient, patient.getUser());

            // list에 추가
            list.add(patientAllSearchResponse);
        }

        // list 반환
        return list;
    }

    // 특정 환자 조회
    public PatientProfileResponse patientProfile(String licenseId, Long patientId) {

        // 의사가 존재하는지 확인
        DoctorEntity doctor = doctorRepository.findByLicenseId(licenseId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 환자가 존재하는지 확인
        PatientEntity patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 담당 의사 + 환자인지 확인
        if (patient.getDoctor() == null || !patient.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new CustomException(DoctorErrorCode.DOCTOR_NO_PERMISSION);
        }

        // entity -> dto
        return patientProfileMapper.toResponse(patient, patient.getUser());
    }
}

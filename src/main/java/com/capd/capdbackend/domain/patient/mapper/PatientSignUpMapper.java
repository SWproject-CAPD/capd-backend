package com.capd.capdbackend.domain.patient.mapper;

import com.capd.capdbackend.domain.patient.dto.request.PatientSignUpRequest;
import com.capd.capdbackend.domain.patient.dto.response.PatientSignUpResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.Role;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class PatientSignUpMapper {

    // dto -> entity (UserEntity부터 변환)
    public UserEntity toUserEntity(PatientSignUpRequest request, String encodedPassword) {
        return UserEntity.builder()
                .userName(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .phone(request.getPhone())
                .role(Role.PATIENT)
                .build();
    }

    // dto -> entity (PatientEntity 변환)
    public PatientEntity toPatientEntity(PatientSignUpRequest request, UserEntity user) {
        return PatientEntity.builder()
                .user(user)
                .sex(request.getSex())
                .age(request.getAge())
                .build();
    }

    // entity -> dto
    public PatientSignUpResponse toResponse(PatientEntity patient, UserEntity user) {
        return PatientSignUpResponse.builder()
                .patientId(patient.getPatientId())
                .userId(user.getUserId())
                .name(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(patient.getSex())
                .age(patient.getAge())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

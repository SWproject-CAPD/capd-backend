package com.capd.capdbackend.domain.doctor.mapper;

import com.capd.capdbackend.domain.doctor.dto.response.PatientProfileResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class PatientProfileMapper {

    // entity -> dto
    public PatientProfileResponse toResponse(PatientEntity patient, UserEntity user) {
        return PatientProfileResponse.builder()
                .patientId(patient.getPatientId())
                .userId(user.getUserId())
                .name(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(patient.getSex())
                .age(patient.getAge())
                .build();
    }
}

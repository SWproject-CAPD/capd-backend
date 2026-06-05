package com.capd.capdbackend.domain.patient.mapper;

import com.capd.capdbackend.domain.patient.dto.response.PatientInfoResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PatientInfoMapper {

    // entity -> dto
    public PatientInfoResponse toResponse(PatientEntity patient, UserEntity user) {
        return PatientInfoResponse.builder()
                .userId(user.getUserId())
                .patientId(patient.getPatientId())
                .name(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(patient.getSex())
                .age(Period.between(patient.getBirthDate(), LocalDate.now()).getYears())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

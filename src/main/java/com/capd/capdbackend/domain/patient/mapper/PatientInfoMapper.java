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
                .age(patient.getBirthDate() != null
                        ? Period.between(patient.getBirthDate(), LocalDate.now()).getYears()
                        : 0) // 기존에 있던 환자는 NULL로 인한 서버 오류 때문에 0으로 반환
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

package com.capd.capdbackend.domain.doctor.mapper;

import com.capd.capdbackend.domain.doctor.dto.request.PatientRegisterRequest;
import com.capd.capdbackend.domain.doctor.dto.response.PatientRegisterResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PatientRegisterMapper {

    // entity -> dto
    public PatientRegisterResponse toResponse(PatientEntity patient, UserEntity user) {
        return PatientRegisterResponse.builder()
                .patientId(patient.getPatientId())
                .userId(user.getUserId())
                .name(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(patient.getSex())
                .age(patient.getBirthDate() != null
                        ? Period.between(patient.getBirthDate(), LocalDate.now()).getYears()
                        : 0) // 기존에 있던 환자는 NULL로 인한 서버 오류 때문에 0으로 반환
                .createdAt(user.getCreatedAt())
                .build();
    }
}

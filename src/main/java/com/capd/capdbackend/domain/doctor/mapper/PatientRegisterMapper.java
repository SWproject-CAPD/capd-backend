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
                .age(Period.between(patient.getBirthDate(), LocalDate.now()).getYears())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

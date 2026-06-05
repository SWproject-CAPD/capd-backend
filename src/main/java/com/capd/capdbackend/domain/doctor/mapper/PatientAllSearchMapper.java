package com.capd.capdbackend.domain.doctor.mapper;

import com.capd.capdbackend.domain.doctor.dto.response.PatientAllSearchResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PatientAllSearchMapper {

    // entity -> dto
    public PatientAllSearchResponse toResponse(PatientEntity patient, UserEntity user) {
        return PatientAllSearchResponse.builder()
                .patientId(patient.getPatientId())
                .userId(user.getUserId())
                .name(user.getUserName())
                .sex(patient.getSex())
                .age(Period.between(patient.getBirthDate(), LocalDate.now()).getYears())
                .build();
    }
}

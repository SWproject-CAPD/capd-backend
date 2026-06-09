package com.capd.capdbackend.domain.doctor.mapper;

import com.capd.capdbackend.domain.doctor.dto.response.PatientPhoneSearchResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PatientPhoneSearchMapper {

    public PatientPhoneSearchResponse toResponse(PatientEntity patient, UserEntity user) {
        return PatientPhoneSearchResponse.builder()
                .patientId(patient.getPatientId())
                .name(user.getUserName())
                .phone(user.getPhone())
                .sex(patient.getSex())
                .age(patient.getBirthDate() != null
                        ? Period.between(patient.getBirthDate(), LocalDate.now()).getYears()
                        : 0)
                .build();
    }
}

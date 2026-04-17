package com.capd.capdbackend.domain.auth.mapper;

import com.capd.capdbackend.domain.auth.dto.response.PatientLoginResponse;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class PatientAuthMapper {

    public PatientLoginResponse toResponse(UserEntity user, PatientEntity patient,String accessToken, Long expireTime) {
        return PatientLoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getUserId())
                .patientId(patient.getPatientId())
                .name(user.getUserName())
                .expiresAt(System.currentTimeMillis() + expireTime)
                .build();
    }
}

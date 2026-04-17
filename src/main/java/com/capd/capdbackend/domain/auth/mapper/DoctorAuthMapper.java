package com.capd.capdbackend.domain.auth.mapper;

import com.capd.capdbackend.domain.auth.dto.response.DoctorLoginResponse;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class DoctorAuthMapper {

    public DoctorLoginResponse toResponse(UserEntity user, DoctorEntity doctor, String accessToken, Long expireTime) {
        return DoctorLoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getUserId())
                .doctorId(doctor.getDoctorId())
                .name(user.getUserName())
                .expiresAt(System.currentTimeMillis() + expireTime)
                .build();
    }
}

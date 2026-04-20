package com.capd.capdbackend.domain.doctor.mapper;

import com.capd.capdbackend.domain.doctor.dto.response.DoctorInfoResponse;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class DoctorInfoMapper {

    // entity -> dto
    public DoctorInfoResponse toResponse(DoctorEntity doctor, UserEntity user) {
        return DoctorInfoResponse.builder()
                .userId(user.getUserId())
                .doctorId(doctor.getDoctorId())
                .name(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

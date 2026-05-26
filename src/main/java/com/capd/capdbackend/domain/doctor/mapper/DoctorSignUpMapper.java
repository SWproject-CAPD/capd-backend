package com.capd.capdbackend.domain.doctor.mapper;

import com.capd.capdbackend.domain.doctor.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.doctor.dto.response.DoctorSignUpResponse;
import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.user.entity.Role;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class DoctorSignUpMapper {

    // dto -> entity (UserEntity부터 변환)
    public UserEntity toUserEntity(DoctorSignUpRequest request, String encodedPassword) {
        return UserEntity.builder()
                .userName(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .phone(request.getPhone())
                .role(Role.DOCTOR)
                .build();
    }

    // dto -> entity (UserEntity 변환후 DoctorEntity)
    public DoctorEntity toDoctorEntity(DoctorSignUpRequest request, UserEntity user) {
        return DoctorEntity.builder()
                .licenseId(request.getLicenseId())
                .user(user)
                .build();
    }

    // entity -> dto
    public DoctorSignUpResponse toResponse(DoctorEntity doctor, UserEntity user) {
        return DoctorSignUpResponse.builder()
                .doctorId(doctor.getDoctorId())
                .userId(user.getUserId())
                .licenseId(doctor.getLicenseId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

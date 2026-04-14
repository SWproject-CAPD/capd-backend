package com.capd.capdbackend.domain.user.mapper;

import com.capd.capdbackend.domain.user.dto.request.DoctorSignUpRequest;
import com.capd.capdbackend.domain.user.dto.response.SignUpResponse;
import com.capd.capdbackend.domain.user.entity.Role;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class DoctorSignUpMapper {

    // dto -> entity
    public UserEntity toEntity(DoctorSignUpRequest request, String encodedPassword) {
        return UserEntity.builder()
                .licenseId(request.getLicenseId())
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .birthdate(request.getBirthdate())
                .phone(request.getPhone())
                .gender(request.getGender())
                .role(Role.DOCTOR)
                .build();
    }

    // entity -> dto
    public SignUpResponse toResponse(UserEntity user) {
        return SignUpResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .birthdate(user.getBirthdate())
                .phone(user.getPhone())
                .gender(user.getGender())
                .createdAt(user.getCreatedAt())
                .role(user.getRole())
                .build();
    }
}

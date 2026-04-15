package com.capd.capdbackend.domain.auth.mapper;

import com.capd.capdbackend.domain.auth.dto.response.LoginResponse;
import com.capd.capdbackend.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public LoginResponse toResponse(UserEntity user, String accessToken, Long expireTime) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole())
                .expiresAt(System.currentTimeMillis() + expireTime)
                .build();
    }
}

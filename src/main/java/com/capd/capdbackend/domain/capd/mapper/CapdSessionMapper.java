package com.capd.capdbackend.domain.capd.mapper;

import com.capd.capdbackend.domain.capd.dto.request.CapdSessionCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdSessionEntity;
import org.springframework.stereotype.Component;

@Component
public class CapdSessionMapper {

    // dto -> entity
    public CapdSessionEntity toSessionEntity(CapdSessionCreateRequest request, CapdCommonEntity common) {

        // 제수량 계산
        float calculateUltrafiltration = request.getDrainVolume() - request.getInfusedFluidWeight();

        return CapdSessionEntity.builder()
                .capdCommon(common)
                .sessionNumber(request.getSessionNumber())
                .exchangeTime(request.getExchangeTime())
                .drainVolume(request.getDrainVolume())
                .dialysateConcentration(request.getDialysateConcentration())
                .infusedFluidWeight(request.getInfusedFluidWeight())
                .ultrafiltration(calculateUltrafiltration)
                .build();
    }

    // entity -> dto
    public CapdSessionResponse toSessionResponse(CapdSessionEntity session) {
        return CapdSessionResponse.builder()
                .capdSessionId(session.getCapdSessionId())
                .sessionNumber(session.getSessionNumber())
                .exchangeTime(session.getExchangeTime())
                .drainVolume(session.getDrainVolume())
                .dialysateConcentration(session.getDialysateConcentration())
                .infusedFluidWeight(session.getInfusedFluidWeight())
                .ultrafiltration(session.getUltrafiltration())
                .build();
    }
}

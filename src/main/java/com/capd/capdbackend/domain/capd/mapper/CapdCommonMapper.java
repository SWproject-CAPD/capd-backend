package com.capd.capdbackend.domain.capd.mapper;

import com.capd.capdbackend.domain.capd.dto.request.CapdCommonCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CapdCommonMapper {

    private final CapdSessionMapper capdSessionMapper;

    // dto -> entity
    public CapdCommonEntity toCommonEntity(CapdCommonCreateRequest request, PatientEntity patient) {
        return CapdCommonEntity.builder()
                .patient(patient)
                .date(request.getDate())
                .cloudyDialysate(request.isCloudyDialysate())
                .urinationCount(request.getUrinationCount())
                .bodyWeight(request.getBodyWeight())
                .bloodPressureSys(request.getBloodPressureSys())
                .bloodPressureDia(request.getBloodPressureDia())
                .fastingBloodSugar(request.getFastingBloodSugar())
                .note(request.getNote())
                .isSubmitted(false)
                .build();
    }

    // entity -> dto
    public CapdCommonResponse toCommonResponse(CapdCommonEntity entity) {

        // session 리스트를 mapper 이용해서 dto list로 변환
        List<CapdSessionResponse> list = entity.getSessions().stream()
                .map(capdSessionMapper::toSessionResponse)
                .collect(Collectors.toList());

        return CapdCommonResponse.builder()
                .capdId(entity.getCapdId())
                .date(entity.getDate())
                .cloudyDialysate(entity.isCloudyDialysate())
                .urinationCount(entity.getUrinationCount())
                .totalUltrafiltration(entity.getTotalUltrafiltration())
                .bodyWeight(entity.getBodyWeight())
                .bloodPressureSys(entity.getBloodPressureSys())
                .bloodPressureDia(entity.getBloodPressureDia())
                .fastingBloodSugar(entity.getFastingBloodSugar())
                .note(entity.getNote())
                .isSubmitted(entity.isSubmitted())
                .sessions(list)
                .build();
    }
}

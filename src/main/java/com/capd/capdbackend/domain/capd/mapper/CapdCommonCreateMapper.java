package com.capd.capdbackend.domain.capd.mapper;

import com.capd.capdbackend.domain.capd.dto.request.CapdCommonCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonCreateResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionCreateResponse;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CapdCommonCreateMapper {

    private final CapdSessionCreateMapper capdSessionCreateMapper;

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
    public CapdCommonCreateResponse toCommonResponse(CapdCommonEntity entity) {

        // session 리스트를 mapper 이용해서 dto list로 변환
        List<CapdSessionCreateResponse> list = entity.getSessions().stream()
                .map(capdSessionCreateMapper::toSessionResponse)
                .collect(Collectors.toList());

        return CapdCommonCreateResponse.builder()
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

package com.capd.capdbackend.domain.capd.mapper;

import com.capd.capdbackend.domain.capd.dto.request.CapdCreateRequest;
import com.capd.capdbackend.domain.capd.dto.response.CapdCommonResponse;
import com.capd.capdbackend.domain.capd.dto.response.CapdSessionResponse;
import com.capd.capdbackend.domain.capd.entity.CapdCommonEntity;
import com.capd.capdbackend.domain.capd.entity.CapdStatus;
import com.capd.capdbackend.domain.patient.entity.PatientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CapdCommonMapper {

    private final CapdSessionMapper capdSessionMapper;

    // dto -> entity (임시저장용)
    public CapdCommonEntity toTempEntity(CapdCreateRequest request, PatientEntity patient) {
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
                .status(CapdStatus.TEMP)
                .build();
    }

    // dto -> entity (최종 제출용)
    public CapdCommonEntity toSubmitEntity(CapdCreateRequest request, PatientEntity patient) {
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
                .status(CapdStatus.SUBMITTED)
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
                .status(entity.getStatus())
                .sessions(list)
                .build();
    }
}

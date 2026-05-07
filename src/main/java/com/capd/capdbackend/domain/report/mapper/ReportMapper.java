package com.capd.capdbackend.domain.report.mapper;

import com.capd.capdbackend.domain.report.dto.response.ReportCreateResponse;
import com.capd.capdbackend.domain.report.entity.ReportEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportCreateResponse toResponse(ReportEntity entity) {
        return ReportCreateResponse.builder()
                .reportId(entity.getReportId())
                .doctorId(entity.getDoctor().getDoctorId())
                .doctorName(entity.getDoctor().getUser().getUserName())
                .patientId(entity.getPatient().getPatientId())
                .patientName(entity.getPatient().getUser().getUserName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .weightSummary(entity.getWeightSummary())
                .bpSummary(entity.getBpSummary())
                .bloodSugarSummary(entity.getBloodSugarSummary())
                .ufSummary(entity.getUfSummary())
                .anomalySummary(entity.getAnomalySummary())
                .docSummary(entity.getDocSummary())
                .build();
    }

}

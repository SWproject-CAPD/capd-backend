package com.capd.capdbackend.domain.reservation.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum ReservationType {

    @Schema(description = "정기 검진 및 결과 상담")
    REGULAR_CHECKUP,

    @Schema(description = "혈액 검사 결과 상담")
    BLOOD_TEST_CONSULTATION,

    @Schema(description = "투석관 점검 및 소독")
    DIALYSIS_TUBE_INSPECTION,

    @Schema(description = "투석액 및 처방 관리")
    PRESCRIPTION_MANAGEMENT
}

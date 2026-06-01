package com.capd.capdbackend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum Role {

    @Schema(description = "의사")
    DOCTOR,

    @Schema(description = "환자")
    PATIENT
}

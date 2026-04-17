package com.capd.capdbackend.domain.patient.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PatientErrorCode implements BaseErrorCode {

    PATIENT_DUPLICATE("P4001", HttpStatus.CONFLICT, "중복된 환자 정보입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}

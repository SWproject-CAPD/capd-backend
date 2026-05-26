package com.capd.capdbackend.domain.doctor.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DoctorErrorCode implements BaseErrorCode {

    LICENSE_ID_NOT_FOUND("D4001", HttpStatus.NOT_FOUND, "존재하지 않는 면허번호입니다."),
    LICENSE_ID_DUPLICATE("D4002", HttpStatus.CONFLICT, "중복된 면허번호입니다."),
    PATIENT_ALREADY_REGISTERED("D4003", HttpStatus.CONFLICT, "이미 등록된 환자입니다."),
    DOCTOR_NO_PERMISSION("D4004", HttpStatus.FORBIDDEN, "접근할 수 없는 환자입니다.");


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

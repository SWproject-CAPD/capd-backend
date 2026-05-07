package com.capd.capdbackend.domain.report.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {

    REPORT_ALREADY_EXISTS("R4001", HttpStatus.CONFLICT, "해당 기간의 보고서가 이미 존재합니다."),
    REPORT_NOT_FOUND("R4002", HttpStatus.NOT_FOUND, "보고서를 찾을 수 없습니다."),
    REPORT_NO_DATA("R4003", HttpStatus.NOT_FOUND, "해당 기간의 투석 데이터가 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

}

package com.capd.capdbackend.domain.capd.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CapdErrorCode implements BaseErrorCode {

    CAPD_NOT_FOUND("C001", HttpStatus.NOT_FOUND, "해당 날짜의 투석 일지를 찾을 수 없습니다."),
    CAPD_SESSION_NOT_FOUND("C002", HttpStatus.NOT_FOUND, "해당 회차의 투석 세션을 찾을 수 없습니다."),
    INVALID_SESSION_NUMBER("C003", HttpStatus.BAD_REQUEST, "유효하지 않은 투석 회차입니다. (1~5 사이의 값을 입력해주세요)"),
    ALREADY_EXIST_SESSION("C004", HttpStatus.CONFLICT, "세션 투석일지가 중복됩니다."),
    ALREADY_SUBMITTED("C005", HttpStatus.CONFLICT, "투석일지를 이미 제출했습니다.");

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

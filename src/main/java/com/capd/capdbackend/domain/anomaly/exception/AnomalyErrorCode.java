package com.capd.capdbackend.domain.anomaly.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AnomalyErrorCode implements BaseErrorCode {

    ANOMALY_SERVER_ERROR("A4001", HttpStatus.SERVICE_UNAVAILABLE, "AI 분석 서버에 연결할 수 없습니다."),
    ANOMALY_NOT_FOUND("A4002", HttpStatus.NOT_FOUND, "이상치 분석 결과를 찾을 수 없습니다.");

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

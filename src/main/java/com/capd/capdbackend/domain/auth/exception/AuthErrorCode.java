package com.capd.capdbackend.domain.auth.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 로그인 관련
    LOGIN_FAIL("AUTH_4001", HttpStatus.BAD_REQUEST, "로그인 처리 중 오류 발생"),
    TOKEN_FAIL("AUTH_4002", HttpStatus.UNAUTHORIZED, "액세스 토큰 요청 실패"),
    USER_INFO_FAIL("AUTH_4003", HttpStatus.UNAUTHORIZED, "사용자 정보 요청 실패"),
    INVALID_ACCESS_TOKEN("AUTH_4004", HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    ACCESS_TOKEN_EXPIRED("AUTH_4005", HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_REQUIRED("AUTH_4006", HttpStatus.FORBIDDEN, "리프레시 토큰이 필요합니다."),
    INVALID_PASSWORD("AUTH_4007", HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 잘못됐습니다."),

    // JWT 토큰 관련
    JWT_TOKEN_EXPIRED("JWT_4001", HttpStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN("JWT_4002", HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 형식입니다."),
    MALFORMED_JWT_TOKEN("JWT_4003", HttpStatus.UNAUTHORIZED, "JWT 형식이 올바르지 않습니다."),
    INVALID_SIGNATURE("JWT_4004", HttpStatus.UNAUTHORIZED, "JWT 서명이 유효하지 않습니다."),
    ILLEGAL_ARGUMENT("JWT_4005", HttpStatus.UNAUTHORIZED, "JWT 토큰 값이 잘못되었습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}

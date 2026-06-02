package com.capd.capdbackend.domain.user.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND("U4001", HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL("U4002", HttpStatus.CONFLICT, "중복된 이메일입니다."),
    DUPLICATE_NICKNAME("U4003", HttpStatus.CONFLICT, "중복된 닉네임입니다."),
    DUPLICATE_PHONE("U4004", HttpStatus.CONFLICT, "중복된 전화번호입니다."),
    PASSWORD_NOT_MATCH("U4005", HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    USER_UNAUTHORIZED("U4006", HttpStatus.UNAUTHORIZED, "회원 정보가 일치하지 않습니다."),
    VERIFICATION_CODE_EXPIRED("U4007", HttpStatus.BAD_REQUEST, "인증 코드가 만료됐습니다."),
    INVALID_VERIFICATION_CODE("U4008", HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    EMAIL_NOT_VERIFIED("U4009", HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다.");

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

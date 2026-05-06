package com.capd.capdbackend.domain.reservation.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements BaseErrorCode {

    RESERVATION_DUPLICATE("R4001", HttpStatus.CONFLICT, "이미 같은 날짜에 예약이 존재합니다."),
    RESERVATION_NOT_FOUND("R4002", HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_NO_PERMISSION("R4003", HttpStatus.FORBIDDEN, "접근할 수 없는 예약입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

}

package com.capd.capdbackend.domain.survey.exception;

import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SurveyErrorCode implements BaseErrorCode {

    QUESTION_NOT_FOUND("S4001", HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다."),
    QUESTION_NO_PERMISSION("S4002", HttpStatus.FORBIDDEN, "접근할 수 없는 질문입니다."),
    QUESTION_ALREADY_ANSWERED("S4003", HttpStatus.CONFLICT, "이미 답변한 질문입니다."),
    QUESTION_NOT_APPROVED("S4004", HttpStatus.BAD_REQUEST, "승인되지 않은 질문입니다."),
    ANSWER_NOT_FOUND("S4005", HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다."),
    ANSWER_DEADLINE_PASSED("S4006", HttpStatus.BAD_REQUEST, "답변 기한이 지났습니다."),
    ANSWER_NO_PERMISSION("S4007", HttpStatus.FORBIDDEN, "답변에 대한 권한이 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}

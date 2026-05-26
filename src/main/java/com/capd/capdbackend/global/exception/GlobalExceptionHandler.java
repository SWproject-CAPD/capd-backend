package com.capd.capdbackend.global.exception;


import com.capd.capdbackend.global.exception.model.BaseErrorCode;
import com.capd.capdbackend.global.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {
        BaseErrorCode errorCode = ex.getErrorCode();
        log.error("Custom 오류 발생: {}", ex.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(BaseResponse.error(errorCode.getStatus().value(), ex.getMessage()));
    }

    // Validation 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String errorMessages =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
                        .collect(Collectors.joining(" / "));
        log.warn("Validation 오류 발생: {}", errorMessages);
        return ResponseEntity.badRequest().body(BaseResponse.error(400, errorMessages));
    }

    // 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
        log.error("Server 오류 발생: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(500, "예상치 못한 서버 오류가 발생했습니다."));
    }

    // auth 파일 만들면 주석 지우기
//    // 비밀번호/아이디 불일치 예외 처리
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<BaseResponse<Void>> handleBadCredentialsException() {
//        // 에러 코드 가져오기
//        AuthErrorCode errorCode = AuthErrorCode.INVALID_PASSWORD;
//
//        // BaseResponse 형식으로 변환해서 반환
//        return ResponseEntity
//                .status(errorCode.getStatus()) // HTTP 상태 코드 (401)
//                .body(BaseResponse.error(
//                        errorCode.getStatus().value(),
//                        errorCode.getMessage()
//                ));
//    }

    // 토큰이 누락되었을 때 발생하는 에러 처리
    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingRequestHeaderException(Exception e) {
        return ResponseEntity
                .badRequest()
                .body(BaseResponse.error(400, "필수 헤더 정보(Authorization)가 누락되었습니다."));
    }

    // JSON 파싱 에러 전용 처리기
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Object>> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.error("JSON 파싱 오류 발생: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(BaseResponse.error(400, "입력 데이터 형식이 잘못되었습니다."));
    }
}

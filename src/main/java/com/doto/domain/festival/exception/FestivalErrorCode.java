package com.doto.domain.festival.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FestivalErrorCode implements ErrorCode {
    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "FESTIVAL-404-001", "축제를 찾을 수 없습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "FESTIVAL-400-001", "커서 값이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    FestivalErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

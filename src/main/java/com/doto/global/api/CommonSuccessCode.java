package com.doto.global.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 특정 도메인에 속하지 않는 공통 성공 코드 */
@Getter
public enum CommonSuccessCode implements SuccessCode {
    OK(HttpStatus.OK, "SUCCESS-200", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "SUCCESS-201", "리소스가 생성되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommonSuccessCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

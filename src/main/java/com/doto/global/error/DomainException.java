package com.doto.global.error;

import lombok.Getter;

/** 도메인 오류를 공통 {@link ErrorCode}와 함께 전달하는 기본 예외 */
@Getter
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    protected DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

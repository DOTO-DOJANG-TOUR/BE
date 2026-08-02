package com.doto.global.error;

import org.springframework.http.HttpStatus;

/** HTTP 상태와 클라이언트용 오류 정보를 정의하는 공통 계약 */
public interface ErrorCode {

    HttpStatus getStatus();

    String getCode();

    String getMessage();
}

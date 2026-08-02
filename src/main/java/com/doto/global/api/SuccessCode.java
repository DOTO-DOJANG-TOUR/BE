package com.doto.global.api;

import org.springframework.http.HttpStatus;

/** HTTP 상태와 클라이언트용 성공 정보를 정의하는 공통 계약 */
public interface SuccessCode {

    HttpStatus getStatus();

    String getCode();

    String getMessage();
}

package com.doto.domain.tourex.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TourApiErrorCode implements ErrorCode {
    TOUR_API_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "TOUR-API-502-001", "관광 정보 서비스를 일시적으로 이용할 수 없습니다."),
    TOUR_API_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, "TOUR-API-502-002", "관광 정보 서비스 응답 처리에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    TourApiErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

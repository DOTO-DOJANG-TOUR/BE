package com.doto.domain.tourspot.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TourErrorCode implements ErrorCode {
    TOUR_SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "TOUR-404-001", "관광지를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    TourErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

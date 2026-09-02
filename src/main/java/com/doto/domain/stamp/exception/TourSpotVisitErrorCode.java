package com.doto.domain.stamp.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TourSpotVisitErrorCode implements ErrorCode {
    TOUR_SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "TOUR-SPOT-404-001", "관광지를 찾을 수 없습니다."),
    ACTIVE_VISIT_NOT_FOUND(HttpStatus.NOT_FOUND, "TOUR-SPOT-VISIT-404-001", "진행 중인 관광지 방문이 없습니다."),
    ACTIVE_VISIT_EXISTS(HttpStatus.CONFLICT, "TOUR-SPOT-VISIT-409-001", "이미 진행 중인 관광지 방문이 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    TourSpotVisitErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

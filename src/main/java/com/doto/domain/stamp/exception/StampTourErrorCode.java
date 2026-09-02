package com.doto.domain.stamp.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StampTourErrorCode implements ErrorCode {
    STAMP_TOUR_NOT_FOUND(HttpStatus.NOT_FOUND, "STAMP-TOUR-404-001", "스탬프 투어를 찾을 수 없습니다."),
    ACTIVE_STAMP_TOUR_EXISTS(HttpStatus.CONFLICT, "STAMP-TOUR-409-001", "해당 축제의 스탬프 투어가 이미 진행 중입니다."),
    ACTIVE_FESTIVAL_VISIT_EXISTS(HttpStatus.CONFLICT, "STAMP-TOUR-409-002", "이미 방문 중인 축제가 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    StampTourErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

package com.doto.domain.stamp.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StampErrorCode implements ErrorCode {
    STAMP_ALREADY_COMPLETED(HttpStatus.CONFLICT, "STAMP-409-001", "이미 완료한 관광지 스탬프입니다."),
    TOUR_SPOT_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "STAMP-400-001", "관광지 반경 300m 이내에서만 도장을 획득할 수 있습니다."),
    TOUR_SPOT_NOT_IN_FESTIVAL(HttpStatus.NOT_FOUND, "STAMP-404-001", "해당 축제의 관광지가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    StampErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

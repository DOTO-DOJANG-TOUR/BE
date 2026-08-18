package com.doto.domain.member.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 사용자 조회/수정 관련 오류 코드 */
@Getter
public enum MemberErrorCode implements ErrorCode {
    INVALID_NICKNAME_LENGTH(HttpStatus.BAD_REQUEST, "MEMBER-400-001", "닉네임은 2자 이상 30자 이어야합니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404-001", "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    MemberErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

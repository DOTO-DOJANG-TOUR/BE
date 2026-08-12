package com.doto.domain.auth.exception;

import com.doto.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 인증(로그인, 회원가입) 관련 오류 코드 */
@Getter
public enum AuthErrorCode implements ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH-409-001", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-401-001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INACTIVE_ACCOUNT(HttpStatus.FORBIDDEN, "AUTH-403-001", "비활성화된 계정입니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "AUTH-401-002", "인증이 필요합니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-003", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-004", "유효하지 않은 소셜 로그인 ID 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

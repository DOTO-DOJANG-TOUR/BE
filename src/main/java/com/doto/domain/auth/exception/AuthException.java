package com.doto.domain.auth.exception;

import com.doto.global.error.DomainException;

public final class AuthException extends DomainException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

}

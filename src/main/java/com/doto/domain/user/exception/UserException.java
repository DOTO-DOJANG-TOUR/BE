package com.doto.domain.user.exception;

import com.doto.global.error.DomainException;

public final class UserException extends DomainException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }

}

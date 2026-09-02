package com.doto.domain.stamp.exception;

import com.doto.global.error.DomainException;

public final class StampException extends DomainException {

    public StampException(StampErrorCode errorCode) {
        super(errorCode);
    }
}

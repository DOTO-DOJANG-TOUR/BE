package com.doto.domain.stamp.exception;

import com.doto.global.error.DomainException;

public final class StampTourException extends DomainException {

    public StampTourException(StampTourErrorCode errorCode) {
        super(errorCode);
    }
}

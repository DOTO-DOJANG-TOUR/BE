package com.doto.domain.tourspot.exception;

import com.doto.global.error.DomainException;

public final class TourException extends DomainException {

    public TourException(TourErrorCode errorCode) {
        super(errorCode);
    }
}

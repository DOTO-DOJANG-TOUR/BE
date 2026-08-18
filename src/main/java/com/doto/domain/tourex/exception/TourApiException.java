package com.doto.domain.tourex.exception;

import com.doto.global.error.DomainException;

public final class TourApiException extends DomainException {

    public TourApiException(TourApiErrorCode errorCode) {
        super(errorCode);
    }
}

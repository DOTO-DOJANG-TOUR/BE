package com.doto.domain.stamp.exception;

import com.doto.global.error.DomainException;

public final class TourSpotVisitException extends DomainException {

    public TourSpotVisitException(TourSpotVisitErrorCode errorCode) {
        super(errorCode);
    }
}

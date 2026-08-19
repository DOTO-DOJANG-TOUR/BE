package com.doto.domain.festival.exception;

import com.doto.global.error.DomainException;

public final class FestivalException extends DomainException {

    public FestivalException(FestivalErrorCode errorCode) {
        super(errorCode);
    }
}

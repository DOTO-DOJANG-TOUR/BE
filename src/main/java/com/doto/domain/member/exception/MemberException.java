package com.doto.domain.member.exception;

import com.doto.global.error.DomainException;

public final class MemberException extends DomainException {

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }

}

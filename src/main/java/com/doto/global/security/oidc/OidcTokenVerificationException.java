package com.doto.global.security.oidc;

/** ID 토큰의 서명, 발급자(iss), 대상(aud), 만료(exp) 검증에 실패했을 때 던진다 */
public class OidcTokenVerificationException extends RuntimeException {

    public OidcTokenVerificationException(String message) {
        super(message);
    }

    public OidcTokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

}

package com.doto.global.security.oidc;

import com.doto.domain.member.entity.SocialProvider;

/** ID Token(OIDC)의 서명, issuer, audience, 만료 시간을 검증하고 회원 식별에 필요한 정보를 추출한다 */
public interface OidcTokenVerifier {

    SocialProvider provider();

    /**
     * ID Token을 검증하고 클레임을 추출한다.
     *
     * @throws com.doto.domain.auth.exception.AuthException 서명, issuer, audience, 만료 시간 등
     *         검증에 실패하면 {@link com.doto.domain.auth.exception.AuthErrorCode#INVALID_SOCIAL_TOKEN}으로 던진다.
     */
    OidcUserInfo verify(String idToken);

}

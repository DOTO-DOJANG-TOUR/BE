package com.doto.global.security.oidc;

import com.doto.domain.member.entity.SocialProvider;

/** ID Token(OIDC)의 서명, issuer, audience, 만료 시간을 검증하고 회원 식별에 필요한 정보를 추출한다 */
public interface OidcTokenVerifier {

    SocialProvider provider();

    /** ID Token을 검증하고 클레임을 추출한다 */
    OidcUserInfo verify(String idToken);

}

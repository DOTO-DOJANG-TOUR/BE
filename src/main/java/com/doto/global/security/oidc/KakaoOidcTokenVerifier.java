package com.doto.global.security.oidc;

import com.doto.domain.member.entity.SocialProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class KakaoOidcTokenVerifier extends AbstractOidcTokenVerifier {

    private static final String CLAIM_NICKNAME = "nickname";

    public KakaoOidcTokenVerifier(@Qualifier("kakaoJwtDecoder") JwtDecoder kakaoJwtDecoder) {
        super(SocialProvider.KAKAO, kakaoJwtDecoder);
    }

    @Override
    protected String extractNickname(Jwt jwt) {
        return jwt.getClaimAsString(CLAIM_NICKNAME);
    }

}

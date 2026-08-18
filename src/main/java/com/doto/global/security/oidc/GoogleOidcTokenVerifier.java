package com.doto.global.security.oidc;

import com.doto.domain.member.entity.SocialProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class GoogleOidcTokenVerifier extends AbstractOidcTokenVerifier {

    private static final String CLAIM_NAME = "name";

    public GoogleOidcTokenVerifier(@Qualifier("googleJwtDecoder") JwtDecoder googleJwtDecoder) {
        super(SocialProvider.GOOGLE, googleJwtDecoder);
    }

    @Override
    protected String extractNickname(Jwt jwt) {
        return jwt.getClaimAsString(CLAIM_NAME);
    }

}

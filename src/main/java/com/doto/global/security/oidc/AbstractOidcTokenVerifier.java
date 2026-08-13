package com.doto.global.security.oidc;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.SocialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.StringUtils;

/**
 * {@link JwtDecoder}로 서명, issuer, audience, 만료 시간을 검증하고 공통 클레임(sub, email, issuer)을
 * 추출한다. 제공자마다 다른 닉네임 클레임 이름만 하위 클래스가 결정한다.
 */
abstract class AbstractOidcTokenVerifier implements OidcTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(AbstractOidcTokenVerifier.class);

    private static final String CLAIM_EMAIL = "email";

    private final SocialProvider provider;
    private final JwtDecoder jwtDecoder;

    protected AbstractOidcTokenVerifier(SocialProvider provider, JwtDecoder jwtDecoder) {
        this.provider = provider;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public SocialProvider provider() {
        return provider;
    }

    @Override
    public OidcUserInfo verify(String idToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(idToken);
        } catch (JwtException e) {
            log.warn("{} ID 토큰 검증 실패: {}", provider, e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        String externalId = jwt.getSubject();
        if (!StringUtils.hasText(externalId)) {
            log.warn("{} ID 토큰에 sub 클레임이 없습니다.", provider);
            throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
        return new OidcUserInfo(externalId, jwt.getClaimAsString(CLAIM_EMAIL), extractNickname(jwt), issuer);
    }

    protected abstract String extractNickname(Jwt jwt);

}

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

/** JWT 서명, issuer, audience, 만료 시간을 검증하고 공통 클레임을 추출*/
abstract class AbstractOidcTokenVerifier implements OidcTokenVerifier {
    // abstract이므로 하위에 생성자 강제됨
    //  JWT 디코딩, sub/email/issuer 추출, 예외 처리가 공통이고 변경할 일이 별로 없어서 사용
    // 서브 클래스는  extractNickname(Jwt) 만 다름 (카카오 vs 구글)

    private static final Logger log = LoggerFactory.getLogger(AbstractOidcTokenVerifier.class);

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_PICTURE = "picture";

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
        // jwt 서명검증
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(idToken);
        } catch (JwtException e) {
            log.warn("{} ID 토큰 검증 실패: {}", provider, e.getMessage());
            throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        // externalid 추출
        String externalId = jwt.getSubject();
        if (!StringUtils.hasText(externalId)) {
            log.warn("{} ID 토큰에 sub 클레임이 없습니다.", provider);
            throw new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        // 사용자 정보 조립
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
        return new OidcUserInfo(
                externalId,
                jwt.getClaimAsString(CLAIM_EMAIL),
                extractNickname(jwt),
                issuer,
                jwt.getClaimAsString(CLAIM_PICTURE)
        );
    }

    protected abstract String extractNickname(Jwt jwt);

}

package com.doto.global.security.oidc;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 소셜 로그인 제공자가 발급한 OIDC ID 토큰을 검증한다.
 *
 * <p>제공자의 JWKS(공개키 목록)로 서명을 검증하고, 발급자(iss)·대상(aud)·만료(exp)가
 * 설정값과 일치하는지 확인한다. 검증에 성공하면 회원 식별에 필요한 클레임만 뽑아 반환한다.
 *
 * <p>제공자별로 issuer/audience(client-id)/jwks-uri가 다르므로, 같은 클래스를 카카오용과
 * 구글용으로 각각 다른 설정 값을 넣어 두 개의 빈으로 등록해서 사용한다.
 */
public class OidcIdTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(OidcIdTokenVerifier.class);

    private static final Duration JWK_SET_CACHE_TTL = Duration.ofHours(1);

    private final String issuer;
    private final String audience;
    private final URL jwksUrl;

    private volatile JWKSet cachedJwkSet;
    private volatile long cachedAtMillis;

    public OidcIdTokenVerifier(String issuer, String audience, String jwksUri) {
        this.issuer = issuer;
        this.audience = audience;
        this.jwksUrl = toUrl(jwksUri);

        if (audience == null || audience.isBlank()) {
            // client-id(REST API 키 / 구글 클라이언트 ID)가 설정 안 된 채로 뜬 것 — 이 상태면
            // 모든 ID 토큰이 대상(aud) 불일치로 무조건 거부된다. 배포/로컬 실행 시 환경변수 누락을 바로 알아채게 경고한다.
            log.warn("OidcIdTokenVerifier(issuer={})의 audience(client-id)가 비어 있습니다. "
                    + "환경변수(KAKAO_OIDC_CLIENT_ID / GOOGLE_OIDC_CLIENT_ID) 설정을 확인하세요.", issuer);
        }
    }

    public OidcClaims verify(String idToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            RSAKey signingKey = findSigningKey(signedJWT.getHeader().getKeyID());

            JWSVerifier verifier = new RSASSAVerifier(signingKey);
            if (!signedJWT.verify(verifier)) {
                throw new OidcTokenVerificationException("ID 토큰 서명이 유효하지 않습니다.");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateClaims(claims);

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new OidcTokenVerificationException("ID 토큰에 sub 클레임이 없습니다.");
            }

            String email = stringClaim(claims, "email");
            String nickname = resolveNickname(claims);
            log.info("ID 토큰 검증 성공 (issuer={}, subject={}): email 클레임 존재={}, nickname 클레임 존재={}",
                    issuer, subject, email != null, nickname != null);

            return new OidcClaims(issuer, subject, email, nickname);
        } catch (OidcTokenVerificationException e) {
            log.warn("ID 토큰 검증 실패 (issuer={}): {}", issuer, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("ID 토큰 검증 중 예외 발생 (issuer={}): {}", issuer, e.toString());
            throw new OidcTokenVerificationException("ID 토큰 검증에 실패했습니다.", e);
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        if (!issuer.equals(claims.getIssuer())) {
            throw new OidcTokenVerificationException(
                    "발급자(iss)가 일치하지 않습니다. expected=%s actual=%s".formatted(issuer, claims.getIssuer()));
        }

        List<String> audiences = claims.getAudience();
        if (audience == null || audience.isBlank() || audiences == null || !audiences.contains(audience)) {
            throw new OidcTokenVerificationException(
                    "대상(aud)이 일치하지 않습니다. expected=%s actual=%s".formatted(audience, audiences));
        }

        Date expiration = claims.getExpirationTime();
        if (expiration == null || expiration.before(new Date())) {
            throw new OidcTokenVerificationException(
                    "ID 토큰이 만료되었습니다. expiration=%s".formatted(expiration));
        }
    }

    private RSAKey findSigningKey(String keyId) {
        JWKSet jwkSet = currentJwkSet();

        JWK jwk = keyId != null ? jwkSet.getKeyByKeyId(keyId) : null;
        if (jwk == null && jwkSet.getKeys().size() == 1) {
            jwk = jwkSet.getKeys().get(0);
        }

        if (!(jwk instanceof RSAKey rsaKey)) {
            throw new OidcTokenVerificationException("ID 토큰 서명에 사용할 공개키를 찾을 수 없습니다.");
        }
        return rsaKey;
    }

    /** 매 요청마다 JWKS를 새로 받아오지 않도록 일정 시간 캐시한다. 키 로테이션 시에는 캐시가 만료되면 자동으로 갱신된다. */
    private synchronized JWKSet currentJwkSet() {
        long now = System.currentTimeMillis();
        if (cachedJwkSet == null || now - cachedAtMillis > JWK_SET_CACHE_TTL.toMillis()) {
            try {
                cachedJwkSet = JWKSet.load(jwksUrl);
                cachedAtMillis = now;
            } catch (Exception e) {
                if (cachedJwkSet != null) {
                    // 일시적인 네트워크 오류라면 만료된 캐시라도 쓰는 편이 완전한 로그인 장애보다 낫다.
                    return cachedJwkSet;
                }
                throw new OidcTokenVerificationException("공개키(JWKS)를 가져오지 못했습니다.", e);
            }
        }
        return cachedJwkSet;
    }

    private static String stringClaim(JWTClaimsSet claims, String name) {
        Object value = claims.getClaim(name);
        return value != null ? value.toString() : null;
    }

    private static String resolveNickname(JWTClaimsSet claims) {
        String nickname = stringClaim(claims, "nickname");
        return nickname != null ? nickname : stringClaim(claims, "name");
    }

    private static URL toUrl(String value) {
        try {
            return URI.create(value).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalArgumentException("jwks-uri 설정이 올바르지 않습니다: " + value, e);
        }
    }

}

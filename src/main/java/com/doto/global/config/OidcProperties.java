package com.doto.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 구글, 카카오 OIDC ID Token 검증에 필요한 issuer, JWK Set URI, client-id를 제공자별로 묶어 관리한다 */
@ConfigurationProperties(prefix = "oidc")
public record OidcProperties(Provider google, Provider kakao) {

    public record Provider(String issuer, String jwkSetUri, String clientId) {
    }

}

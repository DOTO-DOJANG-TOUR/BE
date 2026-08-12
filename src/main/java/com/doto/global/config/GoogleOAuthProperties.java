package com.doto.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 구글 OIDC ID 토큰 검증에 필요한 설정 (Google Cloud Console의 OAuth 클라이언트 ID = client-id) */
@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProperties(String clientId, String issuer, String jwksUri) {
}

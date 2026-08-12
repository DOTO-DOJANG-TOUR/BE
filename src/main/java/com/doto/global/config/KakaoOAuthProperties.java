package com.doto.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 카카오 OIDC ID 토큰 검증에 필요한 설정 (앱 관리 페이지 > 플랫폼 키 > REST API 키 = client-id) */
@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(String clientId, String issuer, String jwksUri) {
}

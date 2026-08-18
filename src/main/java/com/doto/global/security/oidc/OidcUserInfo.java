package com.doto.global.security.oidc;

/** 검증된(verifier) OIDC ID Token에서 추출한, 회원 식별(email)과 기본 프로필 구성에 필요한 정보 */
public record OidcUserInfo(String externalId, String email, String nickname, String issuer, String profileImg) {
}

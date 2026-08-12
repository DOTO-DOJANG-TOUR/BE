package com.doto.global.security.oidc;

/** 서명·발급자·대상·만료 검증을 마친 OIDC ID 토큰에서 뽑아낸 클레임 */
public record OidcClaims(
        String issuer,
        String subject,
        String email,
        String nickname
) {
}

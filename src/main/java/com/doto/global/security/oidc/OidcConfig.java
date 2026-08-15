package com.doto.global.security.oidc;

import com.doto.global.config.OidcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/** 구글, 카카오 ID Token을 검증할 JwtDecoder를 제공자별로 구성한다 */
@Configuration
@EnableConfigurationProperties(OidcProperties.class)
public class OidcConfig {

    @Bean
    public JwtDecoder googleJwtDecoder(OidcProperties oidcProperties) {
        return buildDecoder(oidcProperties.google());
    }

    @Bean
    public JwtDecoder kakaoJwtDecoder(OidcProperties oidcProperties) {
        return buildDecoder(oidcProperties.kakao());
    }

    private JwtDecoder buildDecoder(OidcProperties.Provider provider) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(provider.jwkSetUri()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(provider.issuer()),
                audienceValidator(provider.clientId())
        ));
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String clientId) {
        return jwt -> jwt.getAudience().contains(clientId)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                        "invalid_token", "허용되지 않은 클라이언트에서 발급된 토큰입니다.", null));
    }

}

package com.doto.global.config;

import com.doto.global.security.oidc.OidcIdTokenVerifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KakaoOAuthProperties.class, GoogleOAuthProperties.class})
public class OidcVerifierConfig {

    @Bean
    public OidcIdTokenVerifier kakaoOidcIdTokenVerifier(KakaoOAuthProperties properties) {
        return new OidcIdTokenVerifier(properties.issuer(), properties.clientId(), properties.jwksUri());
    }

    @Bean
    public OidcIdTokenVerifier googleOidcIdTokenVerifier(GoogleOAuthProperties properties) {
        return new OidcIdTokenVerifier(properties.issuer(), properties.clientId(), properties.jwksUri());
    }

}

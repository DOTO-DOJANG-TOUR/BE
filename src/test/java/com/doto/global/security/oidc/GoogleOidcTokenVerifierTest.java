package com.doto.global.security.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.SocialProvider;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
class GoogleOidcTokenVerifierTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private GoogleOidcTokenVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new GoogleOidcTokenVerifier(jwtDecoder);
    }

    @Test
    void provider는_GOOGLE이다() {
        assertThat(verifier.provider()).isEqualTo(SocialProvider.GOOGLE);
    }

    @Nested
    class 성공 {

        @Test
        void 유효한_ID_Token이면_sub_email_name_issuer를_추출한다() {
            Jwt jwt = new Jwt(
                    "id-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of(
                            "sub", "google-sub-123",
                            "iss", "https://accounts.google.com",
                            "email", "member@example.com",
                            "name", "홍길동"
                    )
            );
            when(jwtDecoder.decode("id-token")).thenReturn(jwt);

            OidcUserInfo userInfo = verifier.verify("id-token");

            assertThat(userInfo.externalId()).isEqualTo("google-sub-123");
            assertThat(userInfo.email()).isEqualTo("member@example.com");
            assertThat(userInfo.nickname()).isEqualTo("홍길동");
            assertThat(userInfo.issuer()).isEqualTo("https://accounts.google.com");
        }
    }

    @Nested
    class 실패 {

        @Test
        void 서명_검증에_실패하면_INVALID_SOCIAL_TOKEN_예외를_던진다() {
            when(jwtDecoder.decode("invalid-token")).thenThrow(new JwtException("invalid signature"));

            assertThatThrownBy(() -> verifier.verify("invalid-token"))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }
}

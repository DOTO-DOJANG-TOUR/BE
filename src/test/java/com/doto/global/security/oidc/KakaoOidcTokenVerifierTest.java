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
class KakaoOidcTokenVerifierTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private KakaoOidcTokenVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new KakaoOidcTokenVerifier(jwtDecoder);
    }

    @Test
    void provider는_KAKAO이다() {
        assertThat(verifier.provider()).isEqualTo(SocialProvider.KAKAO);
    }

    @Nested
    class 성공 {

        @Test
        void 유효한_ID_Token이면_sub_email_nickname_issuer를_추출한다() {
            Jwt jwt = new Jwt(
                    "id-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of(
                            "sub", "kakao-sub-123",
                            "iss", "https://kauth.kakao.com",
                            "email", "member@example.com",
                            "nickname", "홍길동"
                    )
            );
            when(jwtDecoder.decode("id-token")).thenReturn(jwt);

            OidcUserInfo userInfo = verifier.verify("id-token");

            assertThat(userInfo.externalId()).isEqualTo("kakao-sub-123");
            assertThat(userInfo.email()).isEqualTo("member@example.com");
            assertThat(userInfo.nickname()).isEqualTo("홍길동");
            assertThat(userInfo.issuer()).isEqualTo("https://kauth.kakao.com");
        }

        @Test
        void 이메일_클레임이_없으면_email은_null이다() {
            Jwt jwt = new Jwt(
                    "id-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of(
                            "sub", "kakao-sub-123",
                            "iss", "https://kauth.kakao.com",
                            "nickname", "홍길동"
                    )
            );
            when(jwtDecoder.decode("id-token")).thenReturn(jwt);

            OidcUserInfo userInfo = verifier.verify("id-token");

            assertThat(userInfo.email()).isNull();
        }
    }

    @Nested
    class 실패 {

        @Test
        void 검증에_실패하면_INVALID_SOCIAL_TOKEN_예외를_던진다() {
            when(jwtDecoder.decode("invalid-token")).thenThrow(new JwtException("invalid audience"));

            assertThatThrownBy(() -> verifier.verify("invalid-token"))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }
}

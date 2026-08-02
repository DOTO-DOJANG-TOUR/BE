package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.user.entity.RefreshToken;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.repository.RefreshTokenRepository;
import com.doto.global.security.OpaqueTokenGenerator;
import com.doto.global.security.jwt.JwtProperties;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties jwtProperties =
            new JwtProperties("test-jwt-secret-key-must-be-long-enough-32bytes", 3600, 1209600);

    private final User user = User.register("홍길동");

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtProperties);
    }

    @Nested
    class 발급 {

        @Test
        void 원문_토큰을_반환하고_해시만_저장한다() {
            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

            String rawToken = refreshTokenService.issue(user);

            verify(refreshTokenRepository).save(captor.capture());
            RefreshToken saved = captor.getValue();
            assertThat(rawToken).isNotBlank();
            assertThat(saved.getTokenHash()).isEqualTo(OpaqueTokenGenerator.hash(rawToken));
            assertThat(saved.getTokenHash()).isNotEqualTo(rawToken);
            assertThat(saved.getUser()).isEqualTo(user);
        }
    }

    @Nested
    class 재발급용_검증 {

        @Test
        void 사용_가능한_토큰이면_검증_후_즉시_폐기한다() {
            String rawToken = "raw-token";
            RefreshToken token =
                    RefreshToken.issue(user, OpaqueTokenGenerator.hash(rawToken), Instant.now().plusSeconds(60));
            when(refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken)))
                    .thenReturn(Optional.of(token));

            RefreshToken result = refreshTokenService.validateAndRevoke(rawToken);

            assertThat(result).isSameAs(token);
            assertThat(token.isUsable()).isFalse();
        }

        @Test
        void 존재하지_않는_토큰이면_예외를_던진다() {
            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.validateAndRevoke("unknown"))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        void 만료된_토큰이면_예외를_던진다() {
            String rawToken = "expired-token";
            RefreshToken token =
                    RefreshToken.issue(user, OpaqueTokenGenerator.hash(rawToken), Instant.now().minusSeconds(1));
            when(refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken)))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.validateAndRevoke(rawToken))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        void 이미_폐기된_토큰이면_예외를_던진다() {
            String rawToken = "revoked-token";
            RefreshToken token =
                    RefreshToken.issue(user, OpaqueTokenGenerator.hash(rawToken), Instant.now().plusSeconds(60));
            token.revoke();
            when(refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken)))
                    .thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.validateAndRevoke(rawToken))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    @Nested
    class 로그아웃용_폐기 {

        @Test
        void 존재하는_토큰은_폐기된다() {
            String rawToken = "raw-token";
            RefreshToken token =
                    RefreshToken.issue(user, OpaqueTokenGenerator.hash(rawToken), Instant.now().plusSeconds(60));
            when(refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken)))
                    .thenReturn(Optional.of(token));

            refreshTokenService.revoke(rawToken);

            assertThat(token.isUsable()).isFalse();
        }

        @Test
        void 존재하지_않는_토큰이어도_예외_없이_끝난다() {
            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

            refreshTokenService.revoke("unknown");

            verify(refreshTokenRepository, never()).save(any());
        }
    }
}

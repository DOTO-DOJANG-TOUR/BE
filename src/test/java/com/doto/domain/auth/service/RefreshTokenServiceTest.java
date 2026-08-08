package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.RefreshToken;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.RefreshTokenRepository;
import com.doto.global.security.HashTokenUtil;
import com.doto.global.config.JwtProperties;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties jwtProperties =
            new JwtProperties("test-jwt-secret-key-must-be-long-enough-32bytes", 3600, 1209600);

    private final Member member = Member.register("홍길동");

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

            String rawToken = refreshTokenService.issue(member);

            verify(refreshTokenRepository).save(captor.capture());
            RefreshToken saved = captor.getValue();
            assertThat(rawToken).isNotBlank();
            assertThat(saved.getTokenHash()).isEqualTo(HashTokenUtil.hash(rawToken));
            assertThat(saved.getTokenHash()).isNotEqualTo(rawToken);
            assertThat(saved.getMember()).isEqualTo(member);
        }
    }

    @Nested
    class 재발급용_검증 {

        @Test
        void 사용_가능한_토큰이면_원자적으로_검증_후_즉시_폐기한다() {
            String rawToken = "raw-token";
            RefreshToken token =
                    RefreshToken.issue(member, HashTokenUtil.hash(rawToken), Instant.now().plusSeconds(60));
            ReflectionTestUtils.setField(token, "id", 1L);
            when(refreshTokenRepository.findByTokenHash(HashTokenUtil.hash(rawToken)))
                    .thenReturn(Optional.of(token));
            when(refreshTokenRepository.revokeIfUsable(eq(1L), any(Instant.class))).thenReturn(1);

            RefreshToken result = refreshTokenService.validateAndRevoke(rawToken);

            assertThat(result).isSameAs(token);
            verify(refreshTokenRepository).revokeIfUsable(eq(1L), any(Instant.class));
        }

        @Test
        void 존재하지_않는_토큰이면_예외를_던진다() {
            when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.validateAndRevoke("unknown"))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);

            verify(refreshTokenRepository, never()).revokeIfUsable(any(), any());
        }

        @Test
        void 원자적_폐기가_0행이면_만료됐거나_이미_폐기된_것으로_보고_예외를_던진다() {
            String rawToken = "expired-or-revoked-token";
            RefreshToken token =
                    RefreshToken.issue(member, HashTokenUtil.hash(rawToken), Instant.now().plusSeconds(60));
            ReflectionTestUtils.setField(token, "id", 1L);
            when(refreshTokenRepository.findByTokenHash(HashTokenUtil.hash(rawToken)))
                    .thenReturn(Optional.of(token));
            when(refreshTokenRepository.revokeIfUsable(eq(1L), any(Instant.class))).thenReturn(0);

            assertThatThrownBy(() -> refreshTokenService.validateAndRevoke(rawToken))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        void 동시에_같은_토큰으로_재발급을_시도하면_한_쪽만_성공한다() {
            String rawToken = "raced-token";
            RefreshToken token =
                    RefreshToken.issue(member, HashTokenUtil.hash(rawToken), Instant.now().plusSeconds(60));
            ReflectionTestUtils.setField(token, "id", 1L);
            when(refreshTokenRepository.findByTokenHash(HashTokenUtil.hash(rawToken)))
                    .thenReturn(Optional.of(token));
            // 원자적 UPDATE이므로 동시에 두 트랜잭션이 요청해도 DB에서 실제로는 하나만 1행을 갱신한다.
            // 여기서는 두 번째 호출이 0행을 갱신하는 경쟁 상황을 모킹으로 재현한다.
            when(refreshTokenRepository.revokeIfUsable(eq(1L), any(Instant.class)))
                    .thenReturn(1)
                    .thenReturn(0);

            refreshTokenService.validateAndRevoke(rawToken);

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
                    RefreshToken.issue(member, HashTokenUtil.hash(rawToken), Instant.now().plusSeconds(60));
            when(refreshTokenRepository.findByTokenHash(HashTokenUtil.hash(rawToken)))
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

package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.RefreshToken;
import com.doto.domain.member.entity.Member;
import com.doto.global.security.jwt.JwtTokenProvider;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshService refreshService;

    private Member activeMember() {
        Member member = Member.register("홍길동");
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }

    @Nested
    class 성공 {

        @Test
        void 유효한_토큰이면_새_토큰_쌍을_발급한다() {
            Member member = activeMember();
            RefreshToken token = RefreshToken.issue(member, "hash", Instant.now().plusSeconds(60));
            RefreshRequestDTO request = new RefreshRequestDTO("raw-refresh-token");

            when(refreshTokenService.validateAndRevoke("raw-refresh-token")).thenReturn(token);
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("new-access-token");
            when(refreshTokenService.issue(member)).thenReturn("new-refresh-token");

            AuthResponseDTO response = refreshService.refresh(request);

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        }
    }

    @Nested
    class 실패 {

        @Test
        void 유효하지_않은_토큰이면_예외가_그대로_전파된다() {
            RefreshRequestDTO request = new RefreshRequestDTO("invalid");
            when(refreshTokenService.validateAndRevoke("invalid"))
                    .thenThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

            assertThatThrownBy(() -> refreshService.refresh(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        void 비활성화된_계정이면_예외를_던진다() {
            Member member = activeMember();
            member.deactivate();
            RefreshToken token = RefreshToken.issue(member, "hash", Instant.now().plusSeconds(60));
            RefreshRequestDTO request = new RefreshRequestDTO("raw-refresh-token");

            when(refreshTokenService.validateAndRevoke("raw-refresh-token")).thenReturn(token);

            assertThatThrownBy(() -> refreshService.refresh(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INACTIVE_ACCOUNT);
        }
    }
}

package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SocialSignInRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.entity.SocialAuthAccount;
import com.doto.domain.member.entity.SocialProvider;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.member.repository.SocialAuthAccountRepository;
import com.doto.global.security.jwt.JwtTokenProvider;
import com.doto.global.security.oidc.OidcTokenVerifier;
import com.doto.global.security.oidc.OidcUserInfo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SocialSignInServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SocialAuthAccountRepository socialAuthAccountRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private OidcTokenVerifier googleVerifier;

    private SocialSignInService socialSignInService;

    @BeforeEach
    void setUp() {
        socialSignInService = new SocialSignInService(
                memberRepository,
                socialAuthAccountRepository,
                jwtTokenProvider,
                refreshTokenService,
                List.of(googleVerifier)
        );
    }

    @Nested
    class 성공 {

        @Test
        void 처음_로그인하는_사용자는_자동으로_회원가입된_후_토큰을_발급받는다() {
            SocialSignInRequestDTO request = new SocialSignInRequestDTO(SocialProvider.GOOGLE, "id-token");
            OidcUserInfo userInfo = new OidcUserInfo(
                    "google-sub-1", "member@example.com", "홍길동", "https://accounts.google.com");

            when(googleVerifier.provider()).thenReturn(SocialProvider.GOOGLE);
            when(googleVerifier.verify("id-token")).thenReturn(userInfo);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.GOOGLE, "google-sub-1"))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                ReflectionTestUtils.setField(member, "id", 1L);
                return member;
            });
            when(socialAuthAccountRepository.save(any(SocialAuthAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(any(Member.class))).thenReturn("refresh-token");

            AuthResponseDTO response = socialSignInService.signIn(request);

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.nickname()).isEqualTo("홍길동");
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            verify(socialAuthAccountRepository).save(any(SocialAuthAccount.class));
        }

        @Test
        void 닉네임_클레임이_없으면_임의의_닉네임으로_회원가입한다() {
            SocialSignInRequestDTO request = new SocialSignInRequestDTO(SocialProvider.GOOGLE, "id-token");
            OidcUserInfo userInfo = new OidcUserInfo(
                    "google-sub-2", "member@example.com", null, "https://accounts.google.com");

            when(googleVerifier.provider()).thenReturn(SocialProvider.GOOGLE);
            when(googleVerifier.verify("id-token")).thenReturn(userInfo);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.GOOGLE, "google-sub-2"))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                ReflectionTestUtils.setField(member, "id", 2L);
                return member;
            });
            when(socialAuthAccountRepository.save(any(SocialAuthAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.createAccessToken(2L)).thenReturn("access-token");
            when(refreshTokenService.issue(any(Member.class))).thenReturn("refresh-token");

            AuthResponseDTO response = socialSignInService.signIn(request);

            assertThat(response.nickname()).isNotBlank();
        }

        @Test
        void 이미_연동된_사용자는_기존_회원으로_토큰을_발급받는다() {
            SocialSignInRequestDTO request = new SocialSignInRequestDTO(SocialProvider.GOOGLE, "id-token");
            OidcUserInfo userInfo = new OidcUserInfo(
                    "google-sub-1", "member@example.com", "홍길동", "https://accounts.google.com");
            Member existingMember = Member.register("기존닉네임");
            ReflectionTestUtils.setField(existingMember, "id", 1L);
            SocialAuthAccount account = SocialAuthAccount.create(
                    existingMember,
                    SocialProvider.GOOGLE,
                    "https://accounts.google.com",
                    "google-sub-1",
                    "member@example.com"
            );

            when(googleVerifier.provider()).thenReturn(SocialProvider.GOOGLE);
            when(googleVerifier.verify("id-token")).thenReturn(userInfo);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.GOOGLE, "google-sub-1"))
                    .thenReturn(Optional.of(account));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(existingMember)).thenReturn("refresh-token");

            AuthResponseDTO response = socialSignInService.signIn(request);

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.nickname()).isEqualTo("기존닉네임");
            verify(memberRepository, never()).save(any());
            verify(socialAuthAccountRepository, never()).save(any());
        }
    }

    @Nested
    class 실패 {

        @Test
        void ID_Token_검증에_실패하면_이후_과정을_진행하지_않는다() {
            SocialSignInRequestDTO request = new SocialSignInRequestDTO(SocialProvider.GOOGLE, "invalid-token");
            when(googleVerifier.provider()).thenReturn(SocialProvider.GOOGLE);
            when(googleVerifier.verify("invalid-token"))
                    .thenThrow(new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN));

            assertThatThrownBy(() -> socialSignInService.signIn(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);

            verifyNoInteractions(memberRepository, socialAuthAccountRepository, jwtTokenProvider, refreshTokenService);
        }

        @Test
        void 지원하지_않는_제공자면_예외를_던지고_검증기를_호출하지_않는다() {
            SocialSignInRequestDTO request = new SocialSignInRequestDTO(SocialProvider.KAKAO, "id-token");

            assertThatThrownBy(() -> socialSignInService.signIn(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);

            verifyNoInteractions(memberRepository, socialAuthAccountRepository, jwtTokenProvider, refreshTokenService);
        }
    }
}

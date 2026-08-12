package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.doto.global.security.oidc.OidcClaims;
import com.doto.global.security.oidc.OidcIdTokenVerifier;
import com.doto.global.security.oidc.OidcTokenVerificationException;
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
    private OidcIdTokenVerifier kakaoOidcIdTokenVerifier;

    @Mock
    private OidcIdTokenVerifier googleOidcIdTokenVerifier;

    // 같은 타입(OidcIdTokenVerifier) 목이 두 개라 @InjectMocks의 타입 기반 매칭이 모호해질 수 있어,
    // 필드 선언 순서에 맞춰 생성자를 직접 호출한다.
    private SocialSignInService socialSignInService;

    @BeforeEach
    void setUp() {
        socialSignInService = new SocialSignInService(
                memberRepository,
                socialAuthAccountRepository,
                jwtTokenProvider,
                refreshTokenService,
                kakaoOidcIdTokenVerifier,
                googleOidcIdTokenVerifier
        );
    }

    private Member activeMember(String nickname) {
        Member member = Member.register(nickname);
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }

    @Nested
    class 성공 {

        @Test
        void 이미_연결된_카카오_계정이면_새로_가입하지_않고_로그인한다() {
            Member member = activeMember("홍길동");
            SocialAuthAccount account = SocialAuthAccount.create(
                    member, SocialProvider.KAKAO, "https://kauth.kakao.com", "kakao-1", "member@kakao.com"
            );
            OidcClaims claims = new OidcClaims("https://kauth.kakao.com", "kakao-1", "member@kakao.com", "홍길동");

            when(kakaoOidcIdTokenVerifier.verify("id-token")).thenReturn(claims);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(account));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(member)).thenReturn("refresh-token");

            AuthResponseDTO response = socialSignInService.kakaoSignIn(new SocialSignInRequestDTO("id-token"));

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            verify(memberRepository, never()).save(any());
        }

        @Test
        void 처음_로그인하는_구글_사용자는_자동으로_회원가입된다() {
            Member newMember = activeMember("Jane");
            OidcClaims claims = new OidcClaims("https://accounts.google.com", "google-1", "member@gmail.com", "Jane");

            when(googleOidcIdTokenVerifier.verify("id-token")).thenReturn(claims);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.GOOGLE, "google-1"))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);
            when(socialAuthAccountRepository.save(any(SocialAuthAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(newMember)).thenReturn("refresh-token");

            AuthResponseDTO response = socialSignInService.googleSignIn(new SocialSignInRequestDTO("id-token"));

            assertThat(response.userId()).isEqualTo("1");
            verify(memberRepository).save(any(Member.class));
            verify(socialAuthAccountRepository).save(any(SocialAuthAccount.class));
        }

        @Test
        void ID_토큰에_닉네임이_없으면_기본_닉네임으로_가입한다() {
            Member newMember = activeMember("카카오사용자");
            OidcClaims claims = new OidcClaims("https://kauth.kakao.com", "kakao-2", null, null);

            when(kakaoOidcIdTokenVerifier.verify("id-token")).thenReturn(claims);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-2"))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);
            when(socialAuthAccountRepository.save(any(SocialAuthAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(newMember)).thenReturn("refresh-token");

            socialSignInService.kakaoSignIn(new SocialSignInRequestDTO("id-token"));

            verify(memberRepository).save(
                    org.mockito.ArgumentMatchers.argThat(member -> member.getNickname().equals("카카오사용자"))
            );
        }

        @Test
        void 재로그인_시_이메일이_새로_채워지면_기존_계정에_반영된다() {
            Member member = activeMember("홍길동");
            SocialAuthAccount account = SocialAuthAccount.create(
                    member, SocialProvider.KAKAO, "https://kauth.kakao.com", "kakao-1", null
            );
            OidcClaims claims = new OidcClaims(
                    "https://kauth.kakao.com", "kakao-1", "new-email@kakao.com", "홍길동"
            );

            when(kakaoOidcIdTokenVerifier.verify("id-token")).thenReturn(claims);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(account));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(member)).thenReturn("refresh-token");

            socialSignInService.kakaoSignIn(new SocialSignInRequestDTO("id-token"));

            assertThat(account.getEmail()).isEqualTo("new-email@kakao.com");
        }

        @Test
        void 재로그인_시_닉네임이_바뀌었으면_회원_닉네임도_갱신된다() {
            Member member = activeMember("옛날닉네임");
            SocialAuthAccount account = SocialAuthAccount.create(
                    member, SocialProvider.KAKAO, "https://kauth.kakao.com", "kakao-1", "member@kakao.com"
            );
            OidcClaims claims = new OidcClaims(
                    "https://kauth.kakao.com", "kakao-1", "member@kakao.com", "새로운닉네임"
            );

            when(kakaoOidcIdTokenVerifier.verify("id-token")).thenReturn(claims);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(account));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(member)).thenReturn("refresh-token");

            socialSignInService.kakaoSignIn(new SocialSignInRequestDTO("id-token"));

            assertThat(member.getNickname()).isEqualTo("새로운닉네임");
        }

        @Test
        void 재로그인_시_클레임에_이메일이_없으면_기존_이메일을_지우지_않는다() {
            Member member = activeMember("홍길동");
            SocialAuthAccount account = SocialAuthAccount.create(
                    member, SocialProvider.KAKAO, "https://kauth.kakao.com", "kakao-1", "kept@kakao.com"
            );
            OidcClaims claims = new OidcClaims("https://kauth.kakao.com", "kakao-1", null, "홍길동");

            when(kakaoOidcIdTokenVerifier.verify("id-token")).thenReturn(claims);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(account));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(member)).thenReturn("refresh-token");

            socialSignInService.kakaoSignIn(new SocialSignInRequestDTO("id-token"));

            assertThat(account.getEmail()).isEqualTo("kept@kakao.com");
        }
    }

    @Nested
    class 실패 {

        @Test
        void ID_토큰_검증에_실패하면_예외를_던진다() {
            when(kakaoOidcIdTokenVerifier.verify("bad-token"))
                    .thenThrow(new OidcTokenVerificationException("서명이 유효하지 않습니다."));

            assertThatThrownBy(() -> socialSignInService.kakaoSignIn(new SocialSignInRequestDTO("bad-token")))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_ID_TOKEN);
        }

        @Test
        void 연결된_회원이_비활성화_상태면_예외를_던진다() {
            Member member = activeMember("홍길동");
            member.deactivate();
            SocialAuthAccount account = SocialAuthAccount.create(
                    member, SocialProvider.KAKAO, "https://kauth.kakao.com", "kakao-1", "member@kakao.com"
            );
            OidcClaims claims = new OidcClaims("https://kauth.kakao.com", "kakao-1", "member@kakao.com", "홍길동");

            when(kakaoOidcIdTokenVerifier.verify("id-token")).thenReturn(claims);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(account));

            assertThatThrownBy(() -> socialSignInService.kakaoSignIn(new SocialSignInRequestDTO("id-token")))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INACTIVE_ACCOUNT);
        }
    }
}

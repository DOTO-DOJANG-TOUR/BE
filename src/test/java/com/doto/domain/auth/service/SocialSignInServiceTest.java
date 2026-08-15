package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
    private OidcTokenVerifier kakaoOidcTokenVerifier;

    @Mock
    private OidcTokenVerifier googleOidcTokenVerifier;

    private SocialSignInService socialSignInService;

    @BeforeEach
    void setUp() {
        // 테스트에 따라 한쪽만 호출될 수 있어 lenient로 스텁
        lenient().when(kakaoOidcTokenVerifier.provider()).thenReturn(SocialProvider.KAKAO);
        lenient().when(googleOidcTokenVerifier.provider()).thenReturn(SocialProvider.GOOGLE);

        socialSignInService = new SocialSignInService(
                memberRepository,
                socialAuthAccountRepository,
                jwtTokenProvider,
                refreshTokenService,
                List.of(kakaoOidcTokenVerifier, googleOidcTokenVerifier)
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
            OidcUserInfo userInfo = new OidcUserInfo(
                    "kakao-1", "member@kakao.com", "홍길동", "https://kauth.kakao.com"
            );

            when(kakaoOidcTokenVerifier.verify("id-token")).thenReturn(userInfo);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(account));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(member)).thenReturn("refresh-token");

            AuthResponseDTO response = socialSignInService.signIn(new SocialSignInRequestDTO(SocialProvider.KAKAO, "id-token"));

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            verify(memberRepository, never()).save(any());
        }

        @Test
        void 처음_로그인하는_구글_사용자는_자동으로_회원가입된다() {
            Member newMember = activeMember("Jane");
            OidcUserInfo userInfo = new OidcUserInfo(
                    "google-1", "member@gmail.com", "Jane", "https://accounts.google.com"
            );

            when(googleOidcTokenVerifier.verify("id-token")).thenReturn(userInfo);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.GOOGLE, "google-1"))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);
            when(socialAuthAccountRepository.save(any(SocialAuthAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(newMember)).thenReturn("refresh-token");

            AuthResponseDTO response = socialSignInService.signIn(new SocialSignInRequestDTO(SocialProvider.GOOGLE, "id-token"));

            assertThat(response.userId()).isEqualTo("1");
            verify(memberRepository).save(any(Member.class));
            verify(socialAuthAccountRepository).save(any(SocialAuthAccount.class));
        }

        @Test
        void ID_토큰에_닉네임이_없으면_기본_닉네임으로_가입한다() {
            Member newMember = activeMember("카카오사용자");
            OidcUserInfo userInfo = new OidcUserInfo("kakao-2", null, null, "https://kauth.kakao.com");

            when(kakaoOidcTokenVerifier.verify("id-token")).thenReturn(userInfo);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-2"))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);
            when(socialAuthAccountRepository.save(any(SocialAuthAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(newMember)).thenReturn("refresh-token");

            socialSignInService.signIn(new SocialSignInRequestDTO(SocialProvider.KAKAO, "id-token"));

            verify(memberRepository).save(
                    org.mockito.ArgumentMatchers.argThat(member -> member.getNickname().equals("카카오사용자"))
            );
        }

    }

    @Nested
    class 실패 {

        @Test
        void ID_토큰_검증에_실패하면_예외가_그대로_전파된다() {
            when(kakaoOidcTokenVerifier.verify("bad-token"))
                    .thenThrow(new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN));

            assertThatThrownBy(() -> socialSignInService.signIn(new SocialSignInRequestDTO(SocialProvider.KAKAO, "bad-token")))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        @Test
        void 연결된_회원이_비활성화_상태면_예외를_던진다() {
            Member member = activeMember("홍길동");
            member.deactivate();
            SocialAuthAccount account = SocialAuthAccount.create(
                    member, SocialProvider.KAKAO, "https://kauth.kakao.com", "kakao-1", "member@kakao.com"
            );
            OidcUserInfo userInfo = new OidcUserInfo(
                    "kakao-1", "member@kakao.com", "홍길동", "https://kauth.kakao.com"
            );

            when(kakaoOidcTokenVerifier.verify("id-token")).thenReturn(userInfo);
            when(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "kakao-1"))
                    .thenReturn(Optional.of(account));

            assertThatThrownBy(() -> socialSignInService.signIn(new SocialSignInRequestDTO(SocialProvider.KAKAO, "id-token")))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INACTIVE_ACCOUNT);
        }
    }
}

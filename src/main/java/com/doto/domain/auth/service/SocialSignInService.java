package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SocialSignInRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.entity.MemberStatus;
import com.doto.domain.member.entity.SocialAuthAccount;
import com.doto.domain.member.entity.SocialProvider;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.member.repository.SocialAuthAccountRepository;
import com.doto.global.security.jwt.JwtTokenProvider;
import com.doto.global.security.oidc.OidcTokenVerifier;
import com.doto.global.security.oidc.OidcUserInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 카카오/구글 ID 토큰(OIDC)을 검증해 로그인 or 자동 회원가입을 처리 */
@Service
@Transactional
@RequiredArgsConstructor
public class SocialSignInService {

    private static final int NICKNAME_MAX_LENGTH = 30;

    private final MemberRepository memberRepository;
    private final SocialAuthAccountRepository socialAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final List<OidcTokenVerifier> oidcTokenVerifiers;

    public AuthResponseDTO signIn(SocialSignInRequestDTO request) {
        SocialProvider provider = request.provider();

        // id 토큰 검증
        OidcUserInfo userInfo = findVerifier(provider).verify(request.idToken());

        // 계정 찾기
        SocialAuthAccount account = socialAuthAccountRepository
                .findByProviderAndExternalId(provider, userInfo.externalId())
                .orElseGet(() -> registerMember(provider, userInfo));

        // 계정 상태 검증
        Member member = account.getMember();
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AuthException(AuthErrorCode.INACTIVE_ACCOUNT);
        }

        // 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = refreshTokenService.issue(member);

        return AuthResponseDTO.of(member, accessToken, refreshToken);
    }

    // OidcTokenverifier(ID 토큰을 검증하는 컴포넌트)반환
    private OidcTokenVerifier findVerifier(SocialProvider provider) {
        return oidcTokenVerifiers.stream()
                .filter(verifier -> verifier.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("등록된 OidcTokenVerifier가 없습니다: " + provider));
    }

    // 회원생성
    private SocialAuthAccount registerMember(SocialProvider provider, OidcUserInfo userInfo) {
        Member member = memberRepository.save(Member.register(resolveNickname(provider, userInfo)));

        return socialAuthAccountRepository.save(
                SocialAuthAccount.create(
                        member, provider, userInfo.issuer(), userInfo.externalId(),
                        userInfo.email(), userInfo.profileImg()
                )
        );
    }


    private String resolveNickname(SocialProvider provider, OidcUserInfo userInfo) {
        String nickname = userInfo.nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = (provider == SocialProvider.KAKAO ? "카카오" : "구글") + "사용자";
        }
        return truncateNickname(nickname);
    }

    private String truncateNickname(String nickname) {
        if (nickname.codePointCount(0, nickname.length()) <= NICKNAME_MAX_LENGTH) {
            return nickname;
        }
        int cutIndex = nickname.offsetByCodePoints(0, NICKNAME_MAX_LENGTH);
        return nickname.substring(0, cutIndex);
    }

}

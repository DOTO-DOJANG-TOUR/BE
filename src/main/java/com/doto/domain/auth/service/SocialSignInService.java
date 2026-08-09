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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 구글, 카카오 등 OIDC ID Token 기반 소셜 로그인을 처리한다.
 *
 * <p>제공자 + 외부 ID로 연동된 계정이 없으면 최초 로그인으로 간주해 Member와 SocialAuthAccount를
 * 함께 생성한다(회원가입과 로그인을 하나의 행위로 묶은 유즈케이스). 이메일이 같다는 이유로 기존
 * 일반/다른 소셜 계정과 자동으로 합치지 않는다. ID Token의 이메일 검증 여부를 신뢰할 수 없는 경우가
 * 있어, 검증되지 않은 이메일 일치만으로 계정을 연결하면 계정 탈취로 이어질 수 있기 때문이다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SocialSignInService {

    private static final int GENERATED_NICKNAME_SUFFIX_LENGTH = 8;
    private static final int NICKNAME_MAX_LENGTH = 30;

    private final MemberRepository memberRepository;
    private final SocialAuthAccountRepository socialAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final List<OidcTokenVerifier> oidcTokenVerifiers;

    public AuthResponseDTO signIn(SocialSignInRequestDTO request) {
        OidcTokenVerifier verifier = verifierFor(request.provider());
        OidcUserInfo userInfo = verifier.verify(request.idToken());

        SocialAuthAccount account = socialAuthAccountRepository
                .findByProviderAndExternalId(request.provider(), userInfo.externalId())
                .orElseGet(() -> registerNewMember(request.provider(), userInfo));

        Member member = account.getMember();
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AuthException(AuthErrorCode.INACTIVE_ACCOUNT);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = refreshTokenService.issue(member);

        return AuthResponseDTO.of(member, accessToken, refreshToken);
    }

    private SocialAuthAccount registerNewMember(SocialProvider provider, OidcUserInfo userInfo) {
        Member member = memberRepository.save(Member.register(resolveNickname(userInfo.nickname())));
        SocialAuthAccount account = SocialAuthAccount.create(
                member,
                provider,
                userInfo.issuer(),
                userInfo.externalId(),
                userInfo.email()
        );
        return socialAuthAccountRepository.save(account);
    }

    private String resolveNickname(String claimedNickname) {
        if (!StringUtils.hasText(claimedNickname)) {
            return generateNickname();
        }
        String trimmed = claimedNickname.trim();
        return trimmed.length() > NICKNAME_MAX_LENGTH ? trimmed.substring(0, NICKNAME_MAX_LENGTH) : trimmed;
    }

    private String generateNickname() {
        return "user_" + UUID.randomUUID().toString().substring(0, GENERATED_NICKNAME_SUFFIX_LENGTH);
    }

    private OidcTokenVerifier verifierFor(SocialProvider provider) {
        return oidcTokenVerifiers.stream()
                .filter(verifier -> verifier.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_SOCIAL_TOKEN));
    }

}

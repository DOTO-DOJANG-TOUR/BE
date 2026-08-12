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
import com.doto.global.security.oidc.OidcClaims;
import com.doto.global.security.oidc.OidcIdTokenVerifier;
import com.doto.global.security.oidc.OidcTokenVerificationException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카카오/구글 ID 토큰(OIDC)을 검증해 로그인 또는 회원가입을 처리한다.
 *
 * <p>해당 provider + external_id로 연결된 계정이 이미 있으면 로그인, 없으면 새 회원을 만들어
 * 연결한다(자동 가입). 이메일/비밀번호 계정과의 병합은 하지 않는다 — 같은 이메일이라도
 * provider가 다르면 별개의 SocialAuthAccount로 취급한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SocialSignInService {

    private static final int NICKNAME_MAX_LENGTH = 30;

    private final MemberRepository memberRepository;
    private final SocialAuthAccountRepository socialAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Qualifier("kakaoOidcIdTokenVerifier")
    private final OidcIdTokenVerifier kakaoOidcIdTokenVerifier;

    @Qualifier("googleOidcIdTokenVerifier")
    private final OidcIdTokenVerifier googleOidcIdTokenVerifier;

    public AuthResponseDTO kakaoSignIn(SocialSignInRequestDTO request) {
        return signIn(SocialProvider.KAKAO, kakaoOidcIdTokenVerifier, request.idToken());
    }

    public AuthResponseDTO googleSignIn(SocialSignInRequestDTO request) {
        return signIn(SocialProvider.GOOGLE, googleOidcIdTokenVerifier, request.idToken());
    }

    private AuthResponseDTO signIn(SocialProvider provider, OidcIdTokenVerifier verifier, String idToken) {
        OidcClaims claims;
        try {
            claims = verifier.verify(idToken);
        } catch (OidcTokenVerificationException e) {
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
        }

        Optional<SocialAuthAccount> existingAccount =
                socialAuthAccountRepository.findByProviderAndExternalId(provider, claims.subject());

        SocialAuthAccount account = existingAccount.orElseGet(() -> registerMember(provider, claims));
        if (existingAccount.isPresent()) {
            // 최초 가입 때만 email/nickname을 저장하면 그 뒤로는 카카오/구글 쪽에서 정보가 바뀌어도
            // (동의항목 재동의로 이메일이 새로 채워지는 경우 포함) 계속 옛날 값으로 남는다.
            // 그래서 로그인할 때마다 최신 클레임으로 다시 맞춰준다.
            syncProfile(account, claims);
        }

        Member member = account.getMember();
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AuthException(AuthErrorCode.INACTIVE_ACCOUNT);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = refreshTokenService.issue(member);

        return AuthResponseDTO.of(member, accessToken, refreshToken);
    }

    private SocialAuthAccount registerMember(SocialProvider provider, OidcClaims claims) {
        Member member = memberRepository.save(Member.register(resolveNickname(provider, claims)));

        return socialAuthAccountRepository.save(
                SocialAuthAccount.create(member, provider, claims.issuer(), claims.subject(), claims.email())
        );
    }

    /**
     * 기존 계정을 최신 클레임으로 갱신한다. 값이 비어 있는 클레임은 무시한다 — 사용자가 이번엔
     * 동의를 안 했다고 해서(또는 제공자가 이번 토큰에 안 실어줬다고 해서) 이미 가진 값을 지우면 안 된다.
     */
    private void syncProfile(SocialAuthAccount account, OidcClaims claims) {
        String email = claims.email();
        if (email != null && !email.isBlank() && !email.equals(account.getEmail())) {
            account.updateEmail(email);
        }

        Member member = account.getMember();
        String nickname = claims.nickname();
        if (nickname != null && !nickname.isBlank()) {
            String truncated = truncateNickname(nickname);
            if (!truncated.equals(member.getNickname())) {
                member.updateNickname(truncated);
            }
        }
    }

    private String resolveNickname(SocialProvider provider, OidcClaims claims) {
        String nickname = claims.nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = (provider == SocialProvider.KAKAO ? "카카오" : "구글") + "사용자";
        }
        return truncateNickname(nickname);
    }

    private String truncateNickname(String nickname) {
        return nickname.length() > NICKNAME_MAX_LENGTH ? nickname.substring(0, NICKNAME_MAX_LENGTH) : nickname;
    }

}

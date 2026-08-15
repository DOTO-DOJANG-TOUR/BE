package com.doto.domain.auth.service;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.RefreshToken;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.RefreshTokenRepository;
import com.doto.global.security.HashTokenUtil;
import com.doto.global.config.JwtProperties;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Refresh Token의 발급, 검증과 폐기를 담당한다 */
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public String issue(Member member) {
        String rawToken = HashTokenUtil.generate();
        String tokenHash = HashTokenUtil.hash(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(jwtProperties.refreshExpirationSeconds());

        refreshTokenRepository.save(RefreshToken.issue(member, tokenHash, expiresAt));

        return rawToken;
    }

    /** 재발급용 검증. 정상 토큰은 재사용 방지를 위해 즉시 폐기한다(rotation) */
    public RefreshToken validateAndRevoke(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(HashTokenUtil.hash(rawToken))
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        int revokedRows = refreshTokenRepository.revokeIfUsable(refreshToken.getId(), Instant.now());
        if (revokedRows == 0) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        return refreshToken;
    }

    /** 로그아웃용 폐기. 토큰이 이미 없거나 만료됐어도 예외 없이 조용히 끝난다(멱등). */
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(HashTokenUtil.hash(rawToken))
                .ifPresent(RefreshToken::revoke);
    }

}

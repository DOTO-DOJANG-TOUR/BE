package com.doto.domain.auth.service;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.user.entity.RefreshToken;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.repository.RefreshTokenRepository;
import com.doto.global.security.OpaqueTokenGenerator;
import com.doto.global.security.jwt.JwtProperties;
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

    public String issue(User user) {
        String rawToken = OpaqueTokenGenerator.generate();
        String tokenHash = OpaqueTokenGenerator.hash(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(jwtProperties.refreshExpirationSeconds());

        refreshTokenRepository.save(RefreshToken.issue(user, tokenHash, expiresAt));

        return rawToken;
    }

    /** 재발급용 검증. 사용 불가능한 토큰이면 예외를 던지고, 정상 토큰은 재사용을 막기 위해 즉시 폐기한다(rotation). */
    public RefreshToken validateAndRevoke(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken))
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!refreshToken.isUsable()) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        refreshToken.revoke();
        return refreshToken;
    }

    /** 로그아웃용 폐기. 토큰이 이미 없거나 만료됐어도 예외 없이 조용히 끝난다(멱등). */
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken))
                .ifPresent(RefreshToken::revoke);
    }

}

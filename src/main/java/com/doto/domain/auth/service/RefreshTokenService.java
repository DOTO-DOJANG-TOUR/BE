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

    /**
     * 재발급용 검증. 사용 불가능한 토큰이면 예외를 던지고, 정상 토큰은 재사용을 막기 위해 즉시 폐기한다(rotation).
     *
     * <p>조회 후 상태를 확인하고 다시 저장하는 방식(select-then-update)은 동시에 같은 토큰으로
     * 재발급 요청이 들어오면 둘 다 isUsable() 체크를 통과해버릴 수 있다. 그래서 폐기는
     * {@link RefreshTokenRepository#revokeIfUsable}로 조건부 UPDATE 한 번에 원자적으로 수행하고,
     * 영향받은 행이 없으면(이미 폐기됐거나 만료됨) 예외를 던진다.
     */
    public RefreshToken validateAndRevoke(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken))
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        int revokedRows = refreshTokenRepository.revokeIfUsable(refreshToken.getId(), Instant.now());
        if (revokedRows == 0) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        return refreshToken;
    }

    /** 로그아웃용 폐기. 토큰이 이미 없거나 만료됐어도 예외 없이 조용히 끝난다(멱등). */
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(OpaqueTokenGenerator.hash(rawToken))
                .ifPresent(RefreshToken::revoke);
    }

}

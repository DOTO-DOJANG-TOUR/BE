package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.user.entity.RefreshToken;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.entity.UserStatus;
import com.doto.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshService {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponseDTO refresh(RefreshRequestDTO request) {
        RefreshToken refreshToken = refreshTokenService.validateAndRevoke(request.refreshToken());

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthException(AuthErrorCode.INACTIVE_ACCOUNT);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String newRefreshToken = refreshTokenService.issue(user);

        return AuthResponseDTO.of(user, accessToken, newRefreshToken);
    }

}

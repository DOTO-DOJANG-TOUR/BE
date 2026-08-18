package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.RefreshRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignOutService {

    private final RefreshTokenService refreshTokenService;

    public void signOut(RefreshRequestDTO request) {
        refreshTokenService.revoke(request.refreshToken());
    }

}

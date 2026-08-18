package com.doto.domain.auth.service;

import static org.mockito.Mockito.verify;

import com.doto.domain.auth.dto.RefreshRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SignOutServiceTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private SignOutService signOutService;

    @Test
    void 전달받은_리프레시_토큰을_폐기한다() {
        RefreshRequestDTO request = new RefreshRequestDTO("raw-token");

        signOutService.signOut(request);

        verify(refreshTokenService).revoke("raw-token");
    }
}

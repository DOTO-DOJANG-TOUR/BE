package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.dto.SocialSignInRequestDTO;
import com.doto.domain.member.entity.SocialProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** AuthUseCase는 하위 Service로 올바르게 위임하는지만 검증한다 */
@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private SignUpService signUpService;

    @Mock
    private SignInService signInService;

    @Mock
    private RefreshService refreshService;

    @Mock
    private SignOutService signOutService;

    @Mock
    private SocialSignInService socialSignInService;

    @InjectMocks
    private AuthUseCase authUseCase;

    @Test
    void 회원가입은_SignUpService에_위임한다() {
        SignUpRequestDTO request = new SignUpRequestDTO("member@example.com", "password1", "홍길동");
        AuthResponseDTO expected = new AuthResponseDTO("1", "홍길동", "access", "refresh");
        when(signUpService.signUp(request)).thenReturn(expected);

        AuthResponseDTO result = authUseCase.signUp(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 로그인은_SignInService에_위임한다() {
        SignInRequestDTO request = new SignInRequestDTO("member@example.com", "password1");
        AuthResponseDTO expected = new AuthResponseDTO("1", "홍길동", "access", "refresh");
        when(signInService.signIn(request)).thenReturn(expected);

        AuthResponseDTO result = authUseCase.signIn(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 소셜_로그인은_SocialSignInService에_위임한다() {
        SocialSignInRequestDTO request = new SocialSignInRequestDTO(SocialProvider.KAKAO, "kakao-id-token");
        AuthResponseDTO expected = new AuthResponseDTO("1", "홍길동", "access", "refresh");
        when(socialSignInService.signIn(request)).thenReturn(expected);

        AuthResponseDTO result = authUseCase.socialSignIn(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 재발급은_RefreshService에_위임한다() {
        RefreshRequestDTO request = new RefreshRequestDTO("raw-token");
        AuthResponseDTO expected = new AuthResponseDTO("1", "홍길동", "access", "refresh");
        when(refreshService.refresh(request)).thenReturn(expected);

        AuthResponseDTO result = authUseCase.refresh(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 로그아웃은_SignOutService에_위임한다() {
        RefreshRequestDTO request = new RefreshRequestDTO("raw-token");

        authUseCase.signOut(request);

        verify(signOutService).signOut(request);
    }
}

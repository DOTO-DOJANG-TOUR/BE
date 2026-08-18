package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.dto.SocialSignInRequestDTO;
import com.doto.domain.member.entity.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUseCase { //하나의 usecase에 여러개의 service를 두어 파사드 패턴을 지킨다.

    private final SignUpService signUpService;
    private final SignInService signInService;
    private final RefreshService refreshService;
    private final SignOutService signOutService;
    private final SocialSignInService socialSignInService;

    public AuthResponseDTO signUp(SignUpRequestDTO request) {
        return signUpService.signUp(request);
    }

    public AuthResponseDTO signIn(SignInRequestDTO request) {
        return signInService.signIn(request);
    }

    public AuthResponseDTO socialSignIn(SocialProvider provider, SocialSignInRequestDTO request) {
        return socialSignInService.signIn(provider, request);
    }

    public AuthResponseDTO refresh(RefreshRequestDTO request) {
        return refreshService.refresh(request);
    }

    public void signOut(RefreshRequestDTO request) {
        signOutService.signOut(request);
    }

}

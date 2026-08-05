package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final SignUpService signUpService;
    private final SignInService signInService;
    private final RefreshService refreshService;
    private final SignOutService signOutService;

    public AuthResponseDTO signUp(SignUpRequestDTO request) {
        return signUpService.signUp(request);
    }

    public AuthResponseDTO signIn(SignInRequestDTO request) {
        return signInService.signIn(request);
    }

    public AuthResponseDTO refresh(RefreshRequestDTO request) {
        return refreshService.refresh(request);
    }

    public void signOut(RefreshRequestDTO request) {
        signOutService.signOut(request);
    }

}

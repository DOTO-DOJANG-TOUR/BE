package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final SignUpService signUpService;
    private final SignInService signInService;

    public AuthResponseDTO signUp(SignUpRequestDTO request) {
        return signUpService.signUp(request);
    }

    public AuthResponseDTO signIn(SignInRequestDTO request) {
        return signInService.signIn(request);
    }

}

package com.doto.domain.auth.controller;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.dto.SocialSignInRequestDTO;
import com.doto.domain.auth.service.AuthUseCase;
import com.doto.global.api.CommonResponse;
import com.doto.global.api.CommonSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthUseCase authUseCase;

    @Override
    public ResponseEntity<CommonResponse<AuthResponseDTO>> signUp(SignUpRequestDTO request) {
        AuthResponseDTO result = authUseCase.signUp(request);
        return ResponseEntity.status(CommonSuccessCode.CREATED.getStatus())
                .body(CommonResponse.success(CommonSuccessCode.CREATED, result));
    }

    @Override
    public ResponseEntity<CommonResponse<AuthResponseDTO>> signIn(SignInRequestDTO request) {
        AuthResponseDTO result = authUseCase.signIn(request);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Override
    public ResponseEntity<CommonResponse<AuthResponseDTO>> socialSignIn(SocialSignInRequestDTO request) {
        AuthResponseDTO result = authUseCase.socialSignIn(request);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Override
    public ResponseEntity<CommonResponse<AuthResponseDTO>> refresh(RefreshRequestDTO request) {
        AuthResponseDTO result = authUseCase.refresh(request);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Override
    public ResponseEntity<Void> signOut(RefreshRequestDTO request) {
        authUseCase.signOut(request);
        return ResponseEntity.noContent().build();
    }

}

package com.doto.domain.auth.controller;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.dto.SocialSignInRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.member.entity.SocialProvider;
import com.doto.global.api.CommonResponse;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "JWT 인증 API")
@ApiErrorCodeExamples({AuthErrorCode.class})
public interface AuthApi {

    @Operation(summary = "회원가입")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @PostMapping("/api/v1/auth/sign-up")
    ResponseEntity<CommonResponse<AuthResponseDTO>> signUp(@Valid @RequestBody SignUpRequestDTO request);

    @Operation(summary = "로그인")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/api/v1/auth/sign-in")
    ResponseEntity<CommonResponse<AuthResponseDTO>> signIn(@Valid @RequestBody SignInRequestDTO request);

    @Operation(
            summary = "소셜 로그인",
            description = """
                    카카오/구글 로그인으로 발급받은 ID 토큰(OIDC)을 검증해 로그인 또는 회원가입을 처리합니다.
                    - 클라이언트에서 소셜 로그인 요청 시 scope에 "openid, email, profile"를 포함해 ID 토큰을 발급받아야 합니다.
                    - 처음 로그인하는 사용자는 자동으로 회원가입 처리됩니다.
                    - provider 경로 변수: KAKAO, GOOGLE
                    """
    )
    @ApiResponse(responseCode = "200", description = "로그인/회원가입 성공")
    @PostMapping("/api/v1/auth/social/{provider}")
    ResponseEntity<CommonResponse<AuthResponseDTO>> socialSignIn(
            @Parameter(description = "소셜 로그인 제공자", example = "KAKAO") @PathVariable SocialProvider provider,
            @Valid @RequestBody SocialSignInRequestDTO request
    );

    @Operation(
            summary = "액세스 토큰 재발급",
            description = """
                    Access Token이 만료되면, 기존 회원가입 로그인시 발급받은 refreshtoken을 넣어 
                    새 accessToken과 refreshToken을 한 쌍으로 다시 발급받습니다.
                    - 요청에 쓴 refreshToken은 이 호출로 즉시 폐기됩니다(재사용 방지).
                    """
    )
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @PostMapping("/api/v1/auth/refresh")
    ResponseEntity<CommonResponse<AuthResponseDTO>> refresh(@Valid @RequestBody RefreshRequestDTO request);

    @Operation(
            summary = "로그아웃",
            description = """
                    전달한 refreshToken을 폐기해 더 이상 재발급(refresh)에 쓸 수 없게 만듭니다.
                    - 성공 응답은 204 No Content이며 body가 없습니다.
                    """
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공 (정상 처리, body 없음)")
    @PostMapping("/api/v1/auth/sign-out")
    ResponseEntity<Void> signOut(@Valid @RequestBody RefreshRequestDTO request);

}

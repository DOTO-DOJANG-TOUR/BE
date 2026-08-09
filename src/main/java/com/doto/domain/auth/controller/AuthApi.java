package com.doto.domain.auth.controller;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.dto.SocialSignInRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
            summary = "소셜 로그인(OIDC)",
            description = """
                    구글, 카카오 SDK에서 openid scope로 발급받은 OIDC ID Token으로 로그인합니다.

                    - ID Token의 서명, issuer, audience(client-id), 만료 시간을 서버에서 검증합니다. \
                    검증에 실패하면 401 AUTH-401-004(유효하지 않은 소셜 로그인 토큰입니다)가 납니다.
                    - 해당 제공자로 처음 로그인하는 사용자는 이 호출로 회원가입까지 함께 처리되고, \
                    이후 같은 제공자로 다시 로그인하면 같은 계정으로 연결됩니다.
                    - 이메일이 같다는 이유만으로 기존 일반 로그인 계정이나 다른 제공자 계정과 자동으로 \
                    합쳐지지 않습니다.

                    client-id(서버에 설정된  GOOGLE_OIDC_CLIENT_ID/KAKAO_OIDC_CLIENT_ID와 동일한 값)로 브라우저에서 직접 \
                    로그인해 ID Token을 받아 idToken 필드에 붙여넣으면 됩니다. 다른 client-id로 받은 토큰은 audience가 달라 401(AUTH-401-004)이 납니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "로그인(최초 로그인 시 회원가입 포함) 성공")
    @PostMapping("/api/v1/auth/oidc")
    ResponseEntity<CommonResponse<AuthResponseDTO>> socialSignIn(@Valid @RequestBody SocialSignInRequestDTO request);

    @Operation(
            summary = "액세스 토큰 재발급",
            description = """
                    Access Token이 만료됐거나 곧 만료될 때, sign-up/sign-in에서 받은 refreshToken으로 \
                    새 accessToken과 refreshToken을 한 쌍으로 다시 발급받습니다.

                    - 요청에 쓴 refreshToken은 이 호출로 즉시 폐기됩니다(재사용 방지). \
                    응답으로 온 새 refreshToken으로 클라이언트가 반드시 교체 저장해야 다음 재발급이 가능합니다.
                    - 이미 폐기됐거나 만료된 refreshToken으로 호출하면 401 AUTH-401-003(유효하지 않은 리프레시 토큰입니다)이 납니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @PostMapping("/api/v1/auth/refresh")
    ResponseEntity<CommonResponse<AuthResponseDTO>> refresh(@Valid @RequestBody RefreshRequestDTO request);

    @Operation(
            summary = "로그아웃",
            description = """
                    전달한 refreshToken을 폐기해 더 이상 재발급(refresh)에 쓸 수 없게 만듭니다.

                    - 성공 응답은 204 No Content이며 body가 없습니다. \
                    이 204는 오류가 아니라 "정상적으로 로그아웃 처리됨"을 뜻합니다. \
                    다른 API처럼 isSuccess/code/message가 담긴 JSON은 내려오지 않습니다.
                    """
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공 (정상 처리, body 없음)")
    @PostMapping("/api/v1/auth/sign-out")
    ResponseEntity<Void> signOut(@Valid @RequestBody RefreshRequestDTO request);

}

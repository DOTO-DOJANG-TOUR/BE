package com.doto.domain.auth.controller;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.RefreshRequestDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
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
                    - 이미 폐기됐거나 존재하지 않는 refreshToken을 보내도 에러 없이 204가 납니다(멱등 처리).
                    - Access Token은 JWT라 서버가 즉시 무효화할 수 없고, 발급 시점의 만료 시간까지는 계속 유효합니다.
                    """
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공 (정상 처리, body 없음)")
    @PostMapping("/api/v1/auth/sign-out")
    ResponseEntity<Void> signOut(@Valid @RequestBody RefreshRequestDTO request);

}

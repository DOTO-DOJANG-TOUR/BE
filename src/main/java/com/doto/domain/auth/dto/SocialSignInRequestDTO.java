package com.doto.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SocialSignInRequestDTO(
        @Schema(description = "소셜 로그인 시 발급받은 ID 토큰(OIDC). scope에 openid를 포함해 로그인해야 발급됩니다.")
        @NotBlank(message = "ID 토큰을 입력해 주세요.")
        String idToken
) {
}

package com.doto.domain.auth.dto;

import com.doto.domain.member.entity.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialSignInRequestDTO(
        @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
        @NotNull(message = "provider를 입력해 주세요.")
        SocialProvider provider,

        @Schema(description = "소셜 로그인 시 발급받은 ID 토큰(OIDC). scope에 openid를 포함해 로그인해야 발급됩니다.")
        @NotBlank(message = "ID 토큰을 입력해 주세요.")
        String idToken
) {
}

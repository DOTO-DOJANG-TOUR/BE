package com.doto.domain.auth.dto;

import com.doto.domain.member.entity.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialSignInRequestDTO(
        @Schema(description = "소셜 로그인 제공자", example = "GOOGLE")
        @NotNull(message = "소셜 로그인 제공자를 입력해 주세요.")
        SocialProvider provider,

        @Schema(description = "제공자 SDK(openid scope)로 발급받은 OIDC ID Token")
        @NotBlank(message = "ID Token을 입력해 주세요.")
        String idToken
) {
}

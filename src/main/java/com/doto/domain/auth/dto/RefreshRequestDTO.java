package com.doto.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @Schema(description = "재발급 또는 로그아웃에 사용할 Refresh Token")
        @NotBlank(message = "리프레시 토큰을 입력해 주세요.")
        String refreshToken
) {
}

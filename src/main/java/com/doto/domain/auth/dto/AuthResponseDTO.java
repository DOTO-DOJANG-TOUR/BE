package com.doto.domain.auth.dto;

import com.doto.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponseDTO(
        @Schema(description = "사용자 ID")
        String userId,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "API 요청 시 Authorization: Bearer 헤더에 사용하는 Access Token")
        String accessToken,

        @Schema(description = "Access Token 재발급(POST /api/v1/auth/refresh)과 로그아웃에 사용하는 Refresh Token")
        String refreshToken
) {

    public static AuthResponseDTO of(User user, String accessToken, String refreshToken) {
        return new AuthResponseDTO(
                String.valueOf(user.getId()),
                user.getNickname(),
                accessToken,
                refreshToken
        );
    }

}

package com.doto.domain.auth.dto;

import com.doto.domain.member.entity.Member;
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
    // 모든 스레드·요청이 같은 변수를 공유하는 static 메소드에 of처럼 력만 보고 결과만 반환하는 경우에는 괜찮대요
    public static AuthResponseDTO of(Member member, String accessToken, String refreshToken) {
        return new AuthResponseDTO(
                String.valueOf(member.getId()),
                member.getNickname(),
                accessToken,
                refreshToken
        );
    }

}

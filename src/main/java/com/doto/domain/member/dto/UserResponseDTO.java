package com.doto.domain.member.dto;

import com.doto.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDTO(
        @Schema(description = "사용자 ID")
        String userId,

        @Schema(description = "이메일", example = "testuser@example.com")
        String email,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "계정 상태", example = "ACTIVE")
        String status,

        @Schema(description = "소셜 로그인 제공자", example = "KAKAO")
        String provider,

        @Schema(description = "프로필 이미지 URL")
        @JsonProperty("profile_img")
        String profileImg
) {

    public static UserResponseDTO from(Member member, String email, String provider, String profileImg) {
        return new UserResponseDTO(
                String.valueOf(member.getId()),
                email,
                member.getNickname(),
                member.getStatus().name(),
                provider,
                profileImg
        );
    }

}

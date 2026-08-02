package com.doto.domain.user.dto;

import com.doto.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record UserResponseDTO(
        @Schema(description = "사용자 ID")
        String userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "계정 상태", example = "ACTIVE")
        String status,

        @Schema(description = "가입일시")
        Instant createdAt
) {

    public static UserResponseDTO from(User user, String email) {
        return new UserResponseDTO(
                String.valueOf(user.getId()),
                email,
                user.getNickname(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }

}

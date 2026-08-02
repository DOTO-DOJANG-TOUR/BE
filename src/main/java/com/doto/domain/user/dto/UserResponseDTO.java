package com.doto.domain.user.dto;

import com.doto.domain.user.entity.User;
import java.time.Instant;

public record UserResponseDTO(
        String userId,
        String email,
        String nickname,
        String status,
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

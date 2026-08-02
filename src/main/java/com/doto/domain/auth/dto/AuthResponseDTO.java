package com.doto.domain.auth.dto;

import com.doto.domain.user.entity.User;

public record AuthResponseDTO(
        String userId,
        String nickname,
        TokenDTO token
) {

    public static AuthResponseDTO of(User user, TokenDTO token) {
        return new AuthResponseDTO(
                String.valueOf(user.getId()),
                user.getNickname(),
                token
        );
    }

}

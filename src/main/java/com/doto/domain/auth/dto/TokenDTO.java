package com.doto.domain.auth.dto;

public record TokenDTO(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {

    public static TokenDTO of(String accessToken, long expiresInSeconds) {
        return new TokenDTO(accessToken, "Bearer", expiresInSeconds);
    }

}

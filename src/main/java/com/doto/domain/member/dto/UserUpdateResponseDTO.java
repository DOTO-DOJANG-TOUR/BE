package com.doto.domain.member.dto;

import com.doto.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateResponseDTO(
        @Schema(description = "닉네임", example = "새로운 닉네임")
        String nickname
) {

    public static UserUpdateResponseDTO from(Member member) {
        return new UserUpdateResponseDTO(member.getNickname());
    }

}

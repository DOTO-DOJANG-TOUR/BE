package com.doto.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateRequestDTO(
        @Schema(description = "닉네임 (2자 이상 30자 이하, 변경할 때만 입력)", example = "홍길동")
        String nickname
) {
}

package com.doto.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
        @Schema(description = "닉네임 (2자 이상 30자 이하, 변경할 때만 입력)", example = "홍길동")
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다.")
        String nickname
) {
}

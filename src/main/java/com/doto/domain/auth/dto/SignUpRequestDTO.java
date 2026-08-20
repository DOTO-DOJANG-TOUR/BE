package com.doto.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDTO(
        @Schema(description = "이메일", example = "testuser@example.com")
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 190, message = "이메일은 190자를 초과할 수 없습니다.")
        String email,

        @Schema(description = "비밀번호 (8자 이상 64자 이하)", example = "stringst")
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
        String password,

        @Schema(description = "닉네임 (2자 이상 30자 이하)", example = "홍길동")
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다.")
        String nickname
) {
}

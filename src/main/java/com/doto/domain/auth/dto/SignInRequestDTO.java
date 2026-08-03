package com.doto.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequestDTO(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 190, message = "이메일은 190자를 초과할 수 없습니다.")
        String email,

        @Schema(description = "비밀번호", example = "stringst")
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password
) {
}

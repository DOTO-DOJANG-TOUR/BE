package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record StampTourRewardRequestDTO(
        @Schema(description = "스탬프 투어 QR코드에 담긴 토큰", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotBlank(message = "QR 토큰은 필수입니다")
        String qrToken
) {
}

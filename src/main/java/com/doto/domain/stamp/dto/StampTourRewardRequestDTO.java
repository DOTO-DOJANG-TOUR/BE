package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StampTourRewardRequestDTO(
        @Schema(description = "스탬프 투어 QR코드에 담긴 6자리 보상 코드", example = "048213")
        @NotBlank(message = "보상 코드는 필수입니다")
        @Pattern(regexp = "^[0-9]{6}$", message = "보상 코드는 숫자 6자리여야 합니다")
        String rewardCode
) {
}

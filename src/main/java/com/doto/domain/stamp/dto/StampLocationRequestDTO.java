package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StampLocationRequestDTO(
        @Schema(description = "사용자 현재 위치 경도", example = "126.5112")
        @NotNull(message = "현재 위치 경도는 필수입니다")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        BigDecimal mapX,

        @Schema(description = "사용자 현재 위치 위도", example = "36.3056")
        @NotNull(message = "현재 위치 위도는 필수입니다")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        BigDecimal mapY
) {
}

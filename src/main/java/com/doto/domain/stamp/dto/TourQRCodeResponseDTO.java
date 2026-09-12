package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TourQRCodeResponseDTO(
        @Schema(description = "QR코드 이미지 데이터 URL (PNG, Base64)", example = "data:image/png;base64,iVBORw0KGgo...")
        String qrCodeImageUrl,

        @Schema(description = "QR 스캔이 어려울 때 사용할 6자리 보상 코드 (숫자 문자열)", example = "048213")
        String rewardCode
) {
}

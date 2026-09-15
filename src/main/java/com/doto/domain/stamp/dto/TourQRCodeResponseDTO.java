package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TourQRCodeResponseDTO(
        @Schema(description = "6자리 보상 코드 (숫자 문자열, QR 스캔이 어려울 때 직접 입력용)", example = "048213")
        String rewardCode,

        @Schema(
                description = "QR코드 이미지 데이터 URL (PNG, Base64). "
                        + "일반 카메라로 스캔하면 관리자 보상 처리 화면(https://doto-reward.netlify.app)으로 "
                        + "보상 코드가 쿼리 파라미터(code)에 담겨 바로 연결됩니다.",
                example = "data:image/png;base64,iVBORw0KGgo..."
        )
        String qrCodeImageUrl
) {
}

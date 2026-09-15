package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StampTourRewardPreviewResponseDTO(
        @Schema(description = "보상을 받을 회원 닉네임", example = "김만두")
        String memberNickname,

        @Schema(description = "QR코드에 담긴 6자리 핀번호", example = "058471")
        String pinNumber,

        @Schema(description = "투어 이름", example = "거문도백도 은빛바다 체험행사")
        String tourName
) {
}

package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StampTourRewardResponseDTO(
        @Schema(description = "축제 ID", example = "1234567890123456789")
        String festivalId,

        @Schema(description = "축제 제목", example = "보령 머드축제")
        String festivalTitle,

        @Schema(description = "보상을 받은 회원 닉네임", example = "홍길동")
        String memberNickname
) {
}

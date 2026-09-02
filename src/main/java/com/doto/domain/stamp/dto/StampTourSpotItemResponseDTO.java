package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StampTourSpotItemResponseDTO(
        @Schema(description = "관광지 ID", example = "1234567890123456789")
        String tourSpotId,

        @Schema(description = "관광지명", example = "대천해수욕장")
        String title,

        @Schema(description = "관광지 대표 이미지 URL", example = "https://tong.visitkorea.or.kr/cms/resource/20/4089520_image2_1.jpg")
        String imageUrl,

        @Schema(description = "관광지 주소", example = "충청남도 보령시 머드로 123")
        String address,

        @Schema(description = "경도", example = "126.5112")
        String mapX,

        @Schema(description = "위도", example = "36.3056")
        String mapY,

        @Schema(description = "관광지 카테고리", example = "자연관광지")
        String category,

        @Schema(description = "축제 위치와의 거리", example = "1.2km")
        String distance
) {
}

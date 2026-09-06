package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관광지 이미지")
public record TourSpotImageDTO(
        @Schema(description = "원본 이미지 URL", example = "https://tong.visitkorea.or.kr/cms/resource/20/4089520_image2_1.jpg")
        String imageUrl,

        @Schema(description = "썸네일 이미지 URL")
        String thumbnailUrl,

        @Schema(description = "이미지명")
        String imageName,

        @Schema(description = "TourAPI 이미지 정렬 순번")
        Integer serialNumber
) {
}

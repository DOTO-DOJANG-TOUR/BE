package com.doto.domain.festival.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FestivalRegionResponseDTO(
        @Schema(description = "축제 ID", example = "1234567890123456789")
        String festivalId,

        @Schema(description = "대표 이미지 URL", example = "https://tong.visitkorea.or.kr/cms/resource/20/4089520_image2_1.jpg")
        String imageUrl,

        @Schema(description = "개최 상태")
        FestivalStatus status,

        @Schema(description = "축제명", example = "보령머드축제")
        String title,

        @Schema(description = "군구", example = "거창군")
        String gunguName,

        @Schema(description = "행사 시작일", example = "2026.08.15")
        String eventStartDate,

        @Schema(description = "행사 종료일", example = "2026.08.24")
        String eventEndDate,

        @Schema(description = "축제 카테고리(lclsSystm1 원본 코드)", example = "EV")
        String category
) {
}

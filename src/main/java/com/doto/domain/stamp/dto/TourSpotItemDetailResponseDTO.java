package com.doto.domain.stamp.dto;

import com.doto.domain.tourspot.entity.enums.TourSpotCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record TourSpotItemDetailResponseDTO(
        @Schema(description = "관광지 ID", example = "1234567890123456789")
        Long tourSpotId,

        @Schema(description = "TourAPI 콘텐츠 ID", example = "125405")
        Long contentId,

        @Schema(description = "관광지명", example = "대천해수욕장")
        String title,

        @Schema(description = "관광지 주소", example = "충청남도 보령시 신흑동")
        String address,

        @Schema(description = "관광지 대표 이미지 URL", example = "https://tong.visitkorea.or.kr/cms/resource/20/4089520_image2_1.jpg")
        String imageUrl,

        @Schema(description = "경도", example = "126.5112")
        String mapX,

        @Schema(description = "위도", example = "36.3056")
        String mapY,

        @Schema(description = "관광지 카테고리", example = "자연관광")
        TourSpotCategory tourSpotCategory,

        @Schema(description = "법정동 시군구 코드", example = "451")
        String legalDongSigunguCode,

        @Schema(description = "전화번호", example = "041-932-2023")
        String phone,

        @Schema(description = "TourAPI 수정 일시", example = "20260830120000")
        String apiModifiedAt
) {
}

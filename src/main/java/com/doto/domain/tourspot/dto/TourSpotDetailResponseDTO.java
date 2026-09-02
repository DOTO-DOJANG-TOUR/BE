package com.doto.domain.tourspot.dto;

import com.doto.domain.tourspot.entity.enums.TourSpotCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관광지 상세 정보")
public record TourSpotDetailResponseDTO(
        @Schema(description = "관광지 ID", example = "1234567890123456789")
        String tourSpotId,

        @Schema(description = "관광지명", example = "대천해수욕장")
        String title,

        @Schema(description = "관광지 대표 이미지 URL")
        String imageUrl,

        @Schema(description = "관광지 주소")
        String address,

        @Schema(description = "경도", example = "126.5112")
        String mapX,

        @Schema(description = "위도", example = "36.3056")
        String mapY,

        @Schema(description = "관광지 카테고리", example = "문화")
        TourSpotCategory category,

        @Schema(description = "법정동 시군구 코드", example = "451")
        String legalDongSigunguCode,

        @Schema(description = "전화번호", example = "041-932-2023")
        String phone,

        @Schema(description = "TourAPI 수정 일시", example = "20260830120000")
        String apiModifiedAt
) {
}

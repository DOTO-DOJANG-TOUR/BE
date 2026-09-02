package com.doto.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record StampTourDetailResponseDTO(
        @Schema(description = "축제 ID", example = "1234567890123456789")
        String festivalId,

        @Schema(description = "스탬프 투어 제목", example = "보령 머드축제 스탬프 투어")
        String title,

        @Schema(description = "도장 개수", example = "3")
        Integer stampCount,

        @Schema(description = "스탬프 투어 관광지 리스트")
        List<StampTourSpotItemResponseDTO> tourSpots
) {
}

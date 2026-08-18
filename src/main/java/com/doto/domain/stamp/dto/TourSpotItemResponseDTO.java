package com.doto.domain.stamp.dto;

import com.doto.domain.stamp.entity.enums.TourSpotCategory;

public record TourSpotItemResponseDTO(
        Long tourSpotId,
        Long contentId,
        String title,
        String address,
        String imageUrl,
        String mapX,
        String mapY,
        TourSpotCategory tourSpotCategory,
        String legalDongRegionCode,
        String legalDongSigunguCode,
        String phone,
        String apiModifiedAt
) {
}

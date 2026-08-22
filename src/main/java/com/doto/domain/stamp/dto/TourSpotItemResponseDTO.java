package com.doto.domain.stamp.dto;

import com.doto.domain.tourex.enums.TourApiCategory;

public record TourSpotItemResponseDTO(
        Long tourSpotId,
        Long contentId,
        String title,
        String address,
        String imageUrl,
        String mapX,
        String mapY,
        TourApiCategory tourSpotCategory,
        String legalDongRegionCode,
        String legalDongSigunguCode,
        String phone,
        String apiModifiedAt
) {
}

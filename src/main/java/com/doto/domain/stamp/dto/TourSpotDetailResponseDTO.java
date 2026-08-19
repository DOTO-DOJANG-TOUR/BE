package com.doto.domain.stamp.dto;

import com.doto.domain.stamp.entity.enums.TourSpotCategory;

public record TourSpotDetailResponseDTO(
        Long contentId,
        Long tourSpotId,
        String title,
        TourSpotCategory tourSpotCategory,
        String imageUrl,
        String address,
        String mapX,
        String mapY,
        String legalDongRegionCode,
        String legalDongSigunguCode,
        String tel,
        String homepageUrl
) {
}

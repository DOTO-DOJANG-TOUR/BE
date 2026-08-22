package com.doto.domain.stamp.dto;

import com.doto.domain.tourex.enums.TourApiCategory;

public record TourSpotDetailResponseDTO(
        Long contentId,
        Long tourSpotId,
        String title,
        TourApiCategory tourSpotCategory,
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

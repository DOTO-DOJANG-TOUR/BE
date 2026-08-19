package com.doto.domain.tourex.dto;

import com.doto.domain.festival.entity.enums.FestivalCategory;

/**
 * TourAPI에서 조회한 축제 콘텐츠 응답
 */
public record FestivalApiResponseDTO(
        Long contentId,
        String title,
        String imageUrl,
        String address,
        String phone,
        String mapX,
        String mapY,
        String overview,
        FestivalCategory category,
        String legalDongRegionCode,
        String legalDongSigunguCode,
        String eventStartDate,
        String eventEndDate,
        String operationHours,
        String playTime,
        String spendTime,
        String holiday,
        String fee,
        String discountInfo,
        String parkingInfo,
        String parkingFee,
        String eventPlace,
        String homepageUrl,
        String reservationInfo,
        String reservationUrl,
        String program,
        String subEvent,
        String schedule,
        String sponsor,
        String sponsorPhone,
        String informationPhone,
        String ageLimit
) {
}

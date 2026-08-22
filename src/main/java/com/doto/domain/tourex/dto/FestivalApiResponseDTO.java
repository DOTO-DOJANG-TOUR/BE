package com.doto.domain.tourex.dto;

/**
 * TourAPI에서 조회한 축제 콘텐츠 응답
 * category는 lclsSystm1(분류체계 대분류) 원본 코드, festivalType은 searchFestival2에만 있는 축제유형명(비어있을 수 있음)
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
        String category,
        String festivalType,
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

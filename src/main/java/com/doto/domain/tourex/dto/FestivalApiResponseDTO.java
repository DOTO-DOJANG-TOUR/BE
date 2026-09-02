package com.doto.domain.tourex.dto;

import lombok.Builder;

/**
 * TourAPI에서 조회한 축제 콘텐츠 응답
 * category는 lclsSystm3(축제 소분류)를 FestivalCategory 라벨로 변환한 값(예: "문화관광", 매핑되지 않으면 "기타"), festivalType은 searchFestival2에만 있는 축제유형명(비어있을 수 있음)
 */
@Builder
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
    // TourAPI는 값이 없는 필드를 공백 문자열("", " ")로 내려줄 때가 있어, 모든 문자열 필드를 공백이면 null로 정규화한다
    public FestivalApiResponseDTO {
        title = blankToNull(title);
        imageUrl = blankToNull(imageUrl);
        address = blankToNull(address);
        phone = blankToNull(phone);
        mapX = blankToNull(mapX);
        mapY = blankToNull(mapY);
        overview = blankToNull(overview);
        category = blankToNull(category);
        festivalType = blankToNull(festivalType);
        legalDongRegionCode = blankToNull(legalDongRegionCode);
        legalDongSigunguCode = blankToNull(legalDongSigunguCode);
        eventStartDate = blankToNull(eventStartDate);
        eventEndDate = blankToNull(eventEndDate);
        operationHours = blankToNull(operationHours);
        playTime = blankToNull(playTime);
        spendTime = blankToNull(spendTime);
        holiday = blankToNull(holiday);
        fee = blankToNull(fee);
        discountInfo = blankToNull(discountInfo);
        parkingInfo = blankToNull(parkingInfo);
        parkingFee = blankToNull(parkingFee);
        eventPlace = blankToNull(eventPlace);
        homepageUrl = blankToNull(homepageUrl);
        reservationInfo = blankToNull(reservationInfo);
        reservationUrl = blankToNull(reservationUrl);
        program = blankToNull(program);
        subEvent = blankToNull(subEvent);
        schedule = blankToNull(schedule);
        sponsor = blankToNull(sponsor);
        sponsorPhone = blankToNull(sponsorPhone);
        informationPhone = blankToNull(informationPhone);
        ageLimit = blankToNull(ageLimit);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}

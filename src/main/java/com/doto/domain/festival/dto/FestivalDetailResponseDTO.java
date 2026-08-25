package com.doto.domain.festival.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FestivalDetailResponseDTO(
        @Schema(description = "대표 이미지 URL", example = "https://tong.visitkorea.or.kr/cms/resource/20/4089520_image2_1.jpg")
        String imageUrl,

        @Schema(description = "축제명", example = "영광불갑산상사화축제")
        String title,

        @Schema(description = "개최 상태")
        FestivalStatus status,

        @Schema(description = "축제 카테고리", example = "축제공연행사")
        String category,

        @Schema(description = "주소", example = "전남광주통합특별시 여수시 삼산면 삼호교길 50")
        String address,

        @Schema(description = "전화번호", example = "061-690-7681")
        String phone,

        @Schema(description = "홈페이지 링크")
        String homepageUrl,

        @Schema(description = "축제 소개")
        String summary,

        @Schema(description = "운영시간")
        String operationHours,

        @Schema(description = "쉬는날")
        String restDate,

        @Schema(description = "이용요금")
        String useFee,

        @Schema(description = "주차 요금, 주차 불가능이면 \"불가능\"")
        String parkingFee
) {
}

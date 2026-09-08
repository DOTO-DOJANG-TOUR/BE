package com.doto.domain.tourex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * TourAPI 축제 소개 정보(detailIntro2) 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FestivalIntroApiResponseDTO(
        Response response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(Header header, Body body) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(Items items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(List<FestivalIntroDTO> item) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FestivalIntroDTO(
            String eventstartdate,
            String eventenddate,
            String usetimefestival,
            String playtime,
            String spendtimefestival,
            String restdate,
            String usefee,
            String discountinfofestival,
            String parking,
            String parkingfee,
            String eventplace,
            String eventhomepage,
            String reservation,
            String reservationurl,
            String program,
            String subevent,
            String schedule,
            String sponsor1,
            String sponsor1tel,
            String infocenter,
            String agelimit
    ) {
    }
}

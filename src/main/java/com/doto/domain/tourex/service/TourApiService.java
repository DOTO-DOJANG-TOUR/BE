package com.doto.domain.tourex.service;

import com.doto.domain.stamp.entity.enums.TourSpotCategory;
import com.doto.domain.stamp.dto.TourSpotDetailResponseDTO;
import com.doto.domain.stamp.dto.TourSpotItemResponseDTO;
import com.doto.domain.tourex.client.TourApiClient;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.dto.FestivalIntroApiResponseDTO;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.domain.tourex.exception.TourApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TourApiService {

    private static final int TOUR_SPOT_SEARCH_RADIUS_METERS = 5_000;
    private final TourApiClient tourApiClient;

    // 관광지의 경우 없으면 호출하는 방식, contentId로 판단
    public TourSpotDetailResponseDTO getTourInfo(Long contentId) {
        TourApiResponseDTO.TourContentDTO tour = getContent(contentId);
        return new TourSpotDetailResponseDTO(
                tour.contentId(),
                null,
                tour.title(),
                toTourSpotCategory(tour.lclsSystem1()),
                tour.firstimage() != null ? tour.firstimage() : tour.firstimage2(),
                tour.addr1(),
                tour.mapx(),
                tour.mapy(),
                tour.legalDongRegionCode(),
                tour.legalDongSigunguCode(),
                tour.tel(),
                tour.homepage()
        );
    }

    // 축제 상세 조회
    public FestivalApiResponseDTO getFestivalInfo(Long festivalContentId) {
        TourApiResponseDTO.TourContentDTO festival = getContent(festivalContentId);
        FestivalIntroApiResponseDTO.FestivalIntroDTO intro = getFestivalIntro(festivalContentId);
        return new FestivalApiResponseDTO(
                festival.contentId(),
                festival.title(),
                getImageUrl(festival),
                festival.addr1(),
                festival.tel(),
                festival.mapx(),
                festival.mapy(),
                festival.overview(),
                intro.eventstartdate(),
                intro.eventenddate(),
                intro.usetimefestival(),
                intro.playtime(),
                intro.spendtimefestival(),
                intro.restdate(),
                intro.usefee(),
                intro.discountinfofestival(),
                intro.parking(),
                intro.parkingfee(),
                intro.eventplace(),
                getHomepageUrl(festival.homepage(), intro.eventhomepage()),
                intro.reservation(),
                intro.reservationurl(),
                intro.program(),
                intro.subevent(),
                intro.schedule(),
                intro.sponsor1(),
                intro.sponsor1tel(),
                intro.infocenter(),
                intro.agelimit()
        );
    }

    // 축제 좌표를 기준으로 주변 관광지만 조회
    public List<TourSpotItemResponseDTO> getNearbyTourSpots(Long festivalContentId) {
        TourApiResponseDTO.TourContentDTO festival = getContent(festivalContentId);
        return getNearbyTourSpots(festival);
    }

    // 스케줄러 동기화 대상 축제 목록을 조회
    public List<TourApiResponseDTO.TourContentDTO> getFestivalsForSync(LocalDate eventStartDate) {
        return getContents(tourApiClient.searchFestivals(eventStartDate, null, null, null));
    }

    private List<TourSpotItemResponseDTO> getNearbyTourSpots(TourApiResponseDTO.TourContentDTO festival) {
        BigDecimal longitude = toCoordinate(festival.mapx());
        BigDecimal latitude = toCoordinate(festival.mapy());

        return tourApiClient.getNearbyTourSpots(longitude, latitude, TOUR_SPOT_SEARCH_RADIUS_METERS)
                .stream()
                .map(tourSpot -> new TourSpotItemResponseDTO(
                        null,
                        tourSpot.contentId(),
                        tourSpot.title(),
                        tourSpot.addr1(),
                        getImageUrl(tourSpot),
                        tourSpot.mapx(),
                        tourSpot.mapy(),
                        toTourSpotCategory(tourSpot.lclsSystem1()),
                        tourSpot.legalDongRegionCode(),
                        tourSpot.legalDongSigunguCode(),
                        tourSpot.tel(),
                        tourSpot.modifiedtime()
                ))
                .toList();
    }

    private TourApiResponseDTO.TourContentDTO getContent(Long contentId) {
        TourApiResponseDTO response = tourApiClient.getContentDetail(contentId);
        List<TourApiResponseDTO.TourContentDTO> contents = getContents(response);
        if (contents.isEmpty()) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
        return contents.getFirst();
    }

    private List<TourApiResponseDTO.TourContentDTO> getContents(TourApiResponseDTO response) {
        if (response == null || response.response() == null || response.response().body() == null
                || response.response().body().items() == null || response.response().body().items().item() == null) {
            return List.of();
        }
        return response.response().body().items().item();
    }

    private FestivalIntroApiResponseDTO.FestivalIntroDTO getFestivalIntro(Long contentId) {
        FestivalIntroApiResponseDTO response = tourApiClient.getFestivalIntro(contentId);
        if (response == null || response.response() == null || response.response().body() == null
                || response.response().body().items() == null || response.response().body().items().item() == null
                || response.response().body().items().item().isEmpty()) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
        return response.response().body().items().item().getFirst();
    }

    private String getImageUrl(TourApiResponseDTO.TourContentDTO content) {
        return content.firstimage() != null ? content.firstimage() : content.firstimage2();
    }

    private String getHomepageUrl(String homepage, String eventHomepage) {
        return eventHomepage == null || eventHomepage.isBlank() ? homepage : eventHomepage;
    }

    private BigDecimal toCoordinate(String coordinate) {
        if (coordinate == null) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
        try {
            return new BigDecimal(coordinate);
        } catch (NumberFormatException exception) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
    }

    // 관광지 카테고리 매핑
    private TourSpotCategory toTourSpotCategory(String lclsSystem1) {
        return switch (lclsSystem1) {
            case "VE" -> TourSpotCategory.CULTURE;
            case "HS" -> TourSpotCategory.HISTORY;
            case "NA" -> TourSpotCategory.NATURE;
            case "EX", "LS" -> TourSpotCategory.EXPERIENCE;
            default -> throw new IllegalArgumentException("지원하지 않는 관광지 대분류 코드입니다: " + lclsSystem1);
        };
    }
}

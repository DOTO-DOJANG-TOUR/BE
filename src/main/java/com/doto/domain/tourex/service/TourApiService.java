package com.doto.domain.tourex.service;

import com.doto.domain.stamp.dto.TourSpotDetailResponseDTO;
import com.doto.domain.stamp.dto.TourSpotItemResponseDTO;
import com.doto.domain.tourex.client.TourApiClient;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.dto.FestivalIntroApiResponseDTO;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.enums.TourApiCategory;
import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.domain.tourex.exception.TourApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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
                toTourApiCategory(tour.lclsSystem1()),
                getImageUrl(tour),
                tour.addr1(),
                tour.mapx(),
                tour.mapy(),
                tour.legalDongRegionCode(),
                tour.legalDongSigunguCode(),
                tour.tel(),
                tour.homepage()
        );
    }

    // 축제 상세 조회, festivalType은 searchFestival2(목록 조회) 결과에만 있어 호출부에서 넘겨받음
    public FestivalApiResponseDTO getFestivalInfo(Long festivalContentId, String festivalType) {
        TourApiResponseDTO.TourContentDTO festival = getContent(festivalContentId);
        FestivalIntroApiResponseDTO.FestivalIntroDTO intro = getFestivalIntro(festivalContentId);
        return FestivalApiResponseDTO.builder()
                .contentId(festival.contentId())
                .title(festival.title())
                .imageUrl(getImageUrl(festival))
                .address(festival.addr1())
                .phone(festival.tel())
                .mapX(festival.mapx())
                .mapY(festival.mapy())
                .overview(festival.overview())
                .category(festival.lclsSystem1())
                .festivalType(festivalType)
                .legalDongRegionCode(festival.legalDongRegionCode())
                .legalDongSigunguCode(festival.legalDongSigunguCode())
                .eventStartDate(intro.eventstartdate())
                .eventEndDate(intro.eventenddate())
                .operationHours(intro.usetimefestival())
                .playTime(intro.playtime())
                .spendTime(intro.spendtimefestival())
                .holiday(intro.restdate())
                .fee(intro.usefee())
                .discountInfo(intro.discountinfofestival())
                .parkingInfo(intro.parking())
                .parkingFee(intro.parkingfee())
                .eventPlace(intro.eventplace())
                .homepageUrl(getHomepageUrl(festival.homepage(), intro.eventhomepage()))
                .reservationInfo(intro.reservation())
                .reservationUrl(intro.reservationurl())
                .program(intro.program())
                .subEvent(intro.subevent())
                .schedule(intro.schedule())
                .sponsor(intro.sponsor1())
                .sponsorPhone(intro.sponsor1tel())
                .informationPhone(intro.infocenter())
                .ageLimit(intro.agelimit())
                .build();
    }

    // 축제 좌표를 기준으로 주변 관광지만 조회
    public List<TourSpotItemResponseDTO> getNearbyTourSpots(Long festivalContentId) {
        TourApiResponseDTO.TourContentDTO festival = getContent(festivalContentId);
        return getNearbyTourSpots(festival.mapx(), festival.mapy());
    }

    public List<TourSpotItemResponseDTO> getNearbyTourSpots(String mapX, String mapY) {
        BigDecimal longitude = toCoordinate(mapX);
        BigDecimal latitude = toCoordinate(mapY);
        return toTourSpotItems(tourApiClient.getNearbyTourSpots(longitude, latitude, TOUR_SPOT_SEARCH_RADIUS_METERS));
    }

    // 스케줄러 동기화 대상 축제 목록을 조회, 각 항목에 festivalType(festivaltype) 포함
    public List<TourApiResponseDTO.TourContentDTO> getFestivalsForSync(LocalDate eventStartDate) {
        return getContents(tourApiClient.searchFestivals(eventStartDate, null, null, null));
    }

    // 주변 검색 결과에는 숙박/음식/쇼핑/축제 등 다양한 대분류가 섞여 있어 항목별 카테고리 매핑 후 목록 구성
    private List<TourSpotItemResponseDTO> toTourSpotItems(List<TourApiResponseDTO.TourContentDTO> tourSpots) {
        return tourSpots
                .stream()
                .map(this::toTourSpotItemOrNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private TourSpotItemResponseDTO toTourSpotItemOrNull(TourApiResponseDTO.TourContentDTO tourSpot) {
        TourApiCategory category = TourApiCategory.fromLclsSystem1Code(tourSpot.lclsSystem1());
        if (category == null) {
            return null;
        }
        return new TourSpotItemResponseDTO(
                null,
                tourSpot.contentId(),
                tourSpot.title(),
                tourSpot.addr1(),
                getImageUrl(tourSpot),
                tourSpot.mapx(),
                tourSpot.mapy(),
                category,
                tourSpot.legalDongRegionCode(),
                tourSpot.legalDongSigunguCode(),
                tourSpot.tel(),
                tourSpot.modifiedtime()
        );
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
        return response == null ? List.of() : response.itemsOrEmpty();
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

    // 특정 contentId 단건 조회용, 알려지지 않은 대분류면 데이터 오류로 보고 예외 발생
    private TourApiCategory toTourApiCategory(String lclsSystem1) {
        TourApiCategory category = TourApiCategory.fromLclsSystem1Code(lclsSystem1);
        if (category == null) {
            throw new IllegalArgumentException("지원하지 않는 관광지 대분류 코드입니다: " + lclsSystem1);
        }
        return category;
    }
}

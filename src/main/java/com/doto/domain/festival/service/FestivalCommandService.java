package com.doto.domain.festival.service;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.enums.Region;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.domain.tourex.exception.TourApiException;
import com.doto.global.util.DateTimeUtils;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FestivalCommandService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final FestivalRepository festivalRepository;
    private final Clock applicationClock;

    // 축제를 contentId 기준으로 upsert
    @Transactional
    public void saveFestival(FestivalApiResponseDTO festival) {
        validateTitle(festival.title());
        validateImageUrl(festival.imageUrl());
        Instant eventStartDate = toEventStartInstant(festival.eventStartDate());
        Instant eventEndDate = toEventEndInstant(festival.eventEndDate());
        Point location = toPoint(festival.mapX(), festival.mapY());
        Region legalRegion = Region.fromCode(festival.legalDongRegionCode());
        String legalGungu = festival.legalDongSigunguCode();

        festivalRepository.findByContentId(festival.contentId())
                .ifPresentOrElse(
                        existing -> existing.update(
                                festival.title(),
                                festival.overview(),
                                festival.imageUrl(),
                                festival.homepageUrl(),
                                festival.category(),
                                festival.phone(),
                                festival.address(),
                                festival.playTime(),
                                festival.operationHours(),
                                festival.holiday(),
                                festival.fee(),
                                festival.parkingInfo(),
                                festival.parkingFee(),
                                festival.program(),
                                legalRegion,
                                legalGungu,
                                eventStartDate,
                                eventEndDate,
                                location
                        ),
                        () -> festivalRepository.save(Festival.create(
                                festival.contentId(),
                                festival.title(),
                                festival.overview(),
                                festival.imageUrl(),
                                festival.homepageUrl(),
                                festival.category(),
                                festival.phone(),
                                festival.address(),
                                festival.playTime(),
                                festival.operationHours(),
                                festival.holiday(),
                                festival.fee(),
                                festival.parkingInfo(),
                                festival.parkingFee(),
                                festival.program(),
                                legalRegion,
                                legalGungu,
                                eventStartDate,
                                eventEndDate,
                                location
                        ))
                );
    }

    // 제목 없는 축제는 저장하지 않음 (정렬/커서 기준 컬럼이라 필수)
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
    }

    // 이미지 없는 축제는 저장하지 않음
    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
    }

    // 행사 시작일 파싱
    private Instant toEventStartInstant(String yyyyMMdd) {
        validateEventDate(yyyyMMdd);
        try {
            return DateTimeUtils.startOfDay(yyyyMMdd, applicationClock.getZone());
        } catch (DateTimeParseException exception) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR, exception);
        }
    }

    // 행사 종료일 파싱
    private Instant toEventEndInstant(String yyyyMMdd) {
        validateEventDate(yyyyMMdd);
        try {
            return DateTimeUtils.endOfDay(yyyyMMdd, applicationClock.getZone());
        } catch (DateTimeParseException exception) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR, exception);
        }
    }

    private void validateEventDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
    }

    // 좌표 문자열을 Point로 변환
    private Point toPoint(String mapX, String mapY) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(toCoordinate(mapX), toCoordinate(mapY)));
    }

    private double toCoordinate(String coordinate) {
        if (coordinate == null) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
        try {
            return Double.parseDouble(coordinate);
        } catch (NumberFormatException exception) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR, exception);
        }
    }
}

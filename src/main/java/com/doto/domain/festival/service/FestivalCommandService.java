package com.doto.domain.festival.service;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.domain.tourex.exception.TourApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final FestivalRepository festivalRepository;

    // 축제를 contentId 기준으로 upsert
    @Transactional
    public void saveFestival(FestivalApiResponseDTO festival) {
        Instant eventStartDate = toEventStartInstant(festival.eventStartDate());
        Instant eventEndDate = toEventEndInstant(festival.eventEndDate());
        Point location = toPoint(festival.mapX(), festival.mapY());

        festivalRepository.findByContentId(festival.contentId())
                .ifPresentOrElse(
                        existing -> existing.update(
                                festival.title(),
                                festival.overview(),
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
                                festival.legalDongRegionCode(),
                                festival.legalDongSigunguCode(),
                                eventStartDate,
                                eventEndDate,
                                location
                        ),
                        () -> festivalRepository.save(Festival.create(
                                festival.contentId(),
                                festival.title(),
                                festival.overview(),
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
                                festival.legalDongRegionCode(),
                                festival.legalDongSigunguCode(),
                                eventStartDate,
                                eventEndDate,
                                location
                        ))
                );
    }

    // 행사 시작일 파싱
    private Instant toEventStartInstant(String yyyyMMdd) {
        return parseEventDate(yyyyMMdd).atStartOfDay(KST).toInstant();
    }

    // 행사 종료일 파싱
    private Instant toEventEndInstant(String yyyyMMdd) {
        return parseEventDate(yyyyMMdd).atTime(LocalTime.MAX).atZone(KST).toInstant();
    }

    private LocalDate parseEventDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR);
        }
        try {
            return LocalDate.parse(yyyyMMdd, EVENT_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new TourApiException(TourApiErrorCode.TOUR_API_RESPONSE_ERROR, exception);
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

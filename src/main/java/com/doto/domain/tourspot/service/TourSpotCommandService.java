package com.doto.domain.tourspot.service;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.stamp.dto.TourSpotItemDetailResponseDTO;
import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Service
@Slf4j
@RequiredArgsConstructor
public class TourSpotCommandService {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final TourSpotRepository tourSpotRepository;
    private final FestivalRepository festivalRepository;
    private final FestivalTourSpotRepository festivalTourSpotRepository;


    // TourSpot을 contentId 기준으로 upsert
    @Transactional
    public void saveTourSpots(Long festivalContentId, List<TourSpotItemDetailResponseDTO> tourSpots) {
        Festival festival = festivalRepository.findByContentId(festivalContentId)
                .orElseThrow(() -> new FestivalException(FestivalErrorCode.FESTIVAL_NOT_FOUND));
        log.info("관광지 동기화 시작: festivalContentId={}, requestedCount={}", festivalContentId, tourSpots.size());

        // TourSpot을 contentId 기준으로 upsert
        List<TourSpot> tourSpotsToSave = tourSpots.stream()
                .collect(Collectors.toMap(
                        TourSpotItemDetailResponseDTO::contentId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .map(dto -> tourSpotRepository.findByContentId(dto.contentId())
                        // 존재 시 정보 업데이트
                        .map(existingTourSpot -> {
                            existingTourSpot.update(
                                    dto.title(),
                                    dto.tourSpotCategory().name(),
                                    dto.imageUrl(),
                                    dto.address(),
                                    null,
                                    dto.legalDongSigunguCode(),
                                    dto.phone(),
                                    dto.apiModifiedAt(),
                                    toPoint(dto.mapX(), dto.mapY())
                            );
                            log.debug("관광지 갱신: contentId={}, title={}", dto.contentId(), dto.title());
                            return existingTourSpot;
                        })
                        .orElseGet(() -> {
                            log.debug("관광지 생성: contentId={}, title={}", dto.contentId(), dto.title());
                            return TourSpot.create(
                                    dto.contentId(),
                                    dto.title(),
                                    dto.tourSpotCategory().name(),
                                    dto.imageUrl(),
                                    dto.address(),
                                    null,
                                    dto.legalDongSigunguCode(),
                                    dto.phone(),
                                    dto.apiModifiedAt(),
                                    toPoint(dto.mapX(), dto.mapY())
                            );
                        }))
                .toList();

        List<TourSpot> savedTourSpots = tourSpotRepository.saveAllAndFlush(tourSpotsToSave);
        // 축제와 관광지 관계 저장
        List<FestivalTourSpot> relationsToSave = savedTourSpots.stream()
                .filter(tourSpot -> !festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(
                        festival.getId(), tourSpot.getId()))
                .map(tourSpot -> FestivalTourSpot.create(
                        festival,
                        tourSpot,
                        festivalTourSpotRepository.calculateDistanceMeters(festival.getId(), tourSpot.getId())
                ))
                .toList();
        if (!relationsToSave.isEmpty()) {
            festivalTourSpotRepository.saveAll(relationsToSave);
        }
        log.info("관광지 동기화 완료: festivalContentId={}, upsertedCount={}, relationCreatedCount={}",
                festivalContentId, savedTourSpots.size(), relationsToSave.size());
    }

    private Point toPoint(String mapX, String mapY) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(Double.parseDouble(mapX), Double.parseDouble(mapY)));
    }
}

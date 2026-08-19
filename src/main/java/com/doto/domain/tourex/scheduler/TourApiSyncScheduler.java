package com.doto.domain.tourex.scheduler;

import com.doto.domain.stamp.dto.TourSpotItemResponseDTO;
import com.doto.domain.festival.service.FestivalCommandService;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.exception.TourApiException;
import com.doto.domain.tourex.service.TourApiService;
import com.doto.domain.tourspot.service.TourSpotCommandService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tour-api.sync", name = "enabled", havingValue = "true")
public class TourApiSyncScheduler {

    private final TourApiService tourApiService;
    private final FestivalCommandService festivalCommandService;
    private final TourSpotCommandService tourSpotCommandService;

    // 매일 새벽에 축제와 주변 관광지 동기화
    @Scheduled(cron = "0 0 1 * * *")
    public void synchronizeTourData() {
        List<TourApiResponseDTO.TourContentDTO> festivals = tourApiService.getFestivalsForSync(
                LocalDate.now()
        );
        log.info("TourAPI 축제 동기화 시작: 대상 {}건", festivals.size());

        for (TourApiResponseDTO.TourContentDTO festival : festivals) {
            try {
                FestivalApiResponseDTO festivalDetail = tourApiService.getFestivalInfo(festival.contentId());
                festivalCommandService.saveFestival(festivalDetail);

                List<TourSpotItemResponseDTO> tourSpots = tourApiService.getNearbyTourSpots(
                        festivalDetail.mapX(), festivalDetail.mapY());
                tourSpotCommandService.saveTourSpots(festival.contentId(), tourSpots);

                log.info("TourAPI 축제 동기화 완료: festivalContentId={}, tourSpotCount={}",
                        festival.contentId(), tourSpots.size());
            } catch (TourApiException | IllegalArgumentException exception) {
                log.warn("TourAPI 축제 동기화 실패: festivalContentId={}", festival.contentId(), exception);
            }
        }
    }
}

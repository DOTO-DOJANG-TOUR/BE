package com.doto.domain.tourex.scheduler;

import com.doto.domain.festival.service.FestivalCommandService;
import com.doto.domain.stamp.dto.TourSpotItemDetailResponseDTO;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.service.TourApiService;
import com.doto.domain.tourspot.service.TourSpotCommandService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tour-api.sync", name = "enabled", havingValue = "true")
public class TourApiSyncScheduler {

    private final TourApiService tourApiService;
    private final FestivalCommandService festivalCommandService;
    private final TourSpotCommandService tourSpotCommandService;
    private final Clock applicationClock;

    // 매일 새벽에 축제와 주변 관광지 동기화
    @Scheduled(cron = "0 0 1 * * *")
    public void synchronizeTourData() {
        List<TourApiResponseDTO.TourContentDTO> festivals =
                tourApiService.getFestivalsForSync(LocalDate.now(applicationClock));
        log.info("축제 동기화 시작: 대상 {}건", festivals.size());

        int successCount = 0;
        for (TourApiResponseDTO.TourContentDTO festival : festivals) {
            try {
                FestivalApiResponseDTO festivalDetail = tourApiService.getFestivalInfo(
                        festival.contentId(), festival.festivalType());
                festivalCommandService.saveFestival(festivalDetail);

                List<TourSpotItemDetailResponseDTO> tourSpots = tourApiService.getNearbyTourSpots(
                        festivalDetail.mapX(), festivalDetail.mapY());
                tourSpotCommandService.saveTourSpots(festival.contentId(), tourSpots);

                successCount++;
            } catch (RuntimeException exception) {
                // 한 건이 실패해도 나머지 건은 계속 진행
                log.warn("축제 동기화 실패: festivalContentId={}", festival.contentId(), exception);
            }
        }

        log.info("축제 동기화 완료: 대상 {}건, 성공 {}건", festivals.size(), successCount);
    }
}

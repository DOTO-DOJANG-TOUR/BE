package com.doto.domain.admin.service;

import com.doto.domain.admin.dto.TourSyncResultDTO;
import com.doto.domain.festival.service.FestivalCommandService;
import com.doto.domain.stamp.dto.TourSpotItemResponseDTO;
import com.doto.domain.tourex.dto.FestivalApiResponseDTO;
import com.doto.domain.tourex.dto.TourApiResponseDTO;
import com.doto.domain.tourex.service.TourApiService;
import com.doto.domain.tourspot.service.TourSpotCommandService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 관리자 수동 동기화, TourApiSyncScheduler와 같은 로직을 10건씩 배치로 처리
@Slf4j
@Service
@RequiredArgsConstructor
public class TourSyncAdminService {

    private static final int BATCH_SIZE = 10;

    private final TourApiService tourApiService;
    private final FestivalCommandService festivalCommandService;
    private final TourSpotCommandService tourSpotCommandService;

    public TourSyncResultDTO synchronizeFestivals(LocalDate eventStartDate) {
        LocalDate targetDate = eventStartDate != null ? eventStartDate : LocalDate.now();
        List<TourApiResponseDTO.TourContentDTO> festivals = tourApiService.getFestivalsForSync(targetDate);
        log.info("[관리자] 축제 동기화 시작: 대상일={}, 대상 {}건", targetDate, festivals.size());

        List<List<TourApiResponseDTO.TourContentDTO>> batches = partition(festivals, BATCH_SIZE);
        List<TourSyncResultDTO.BatchResultDTO> batchResults = new ArrayList<>();
        List<Long> failedFestivalContentIds = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < batches.size(); i++) {
            List<TourApiResponseDTO.TourContentDTO> batch = batches.get(i);
            int batchIndex = i + 1;
            int batchSuccessCount = processBatch(batch, failedFestivalContentIds);
            successCount += batchSuccessCount;

            log.info("[관리자] 배치 처리 완료: batchIndex={}/{}, batchSize={}, successCount={}, failedCount={}",
                    batchIndex, batches.size(), batch.size(), batchSuccessCount, batch.size() - batchSuccessCount);
            batchResults.add(new TourSyncResultDTO.BatchResultDTO(
                    batchIndex, batch.size(), batchSuccessCount, batch.size() - batchSuccessCount));
        }

        log.info("[관리자] 축제 동기화 완료: 대상 {}건, 성공 {}건, 실패 {}건",
                festivals.size(), successCount, failedFestivalContentIds.size());

        return new TourSyncResultDTO(
                targetDate,
                festivals.size(),
                BATCH_SIZE,
                successCount,
                failedFestivalContentIds.size(),
                failedFestivalContentIds,
                batchResults
        );
    }

    // 배치 하나를 순차 처리, 한 건이 실패해도 나머지 건은 계속 진행
    private int processBatch(List<TourApiResponseDTO.TourContentDTO> batch, List<Long> failedFestivalContentIds) {
        int batchSuccessCount = 0;
        for (TourApiResponseDTO.TourContentDTO festival : batch) {
            try {
                syncFestival(festival);
                batchSuccessCount++;
            } catch (RuntimeException exception) {
                log.warn("[관리자] 축제 동기화 실패: festivalContentId={}", festival.contentId(), exception);
                failedFestivalContentIds.add(festival.contentId());
            }
        }
        return batchSuccessCount;
    }

    private void syncFestival(TourApiResponseDTO.TourContentDTO festival) {
        FestivalApiResponseDTO festivalDetail = tourApiService.getFestivalInfo(
                festival.contentId(), festival.festivalType());
        festivalCommandService.saveFestival(festivalDetail);

        List<TourSpotItemResponseDTO> tourSpots = tourApiService.getNearbyTourSpots(
                festivalDetail.mapX(), festivalDetail.mapY());
        tourSpotCommandService.saveTourSpots(festival.contentId(), tourSpots);
    }

    // 대상 목록을 batchSize 단위로 분할
    private List<List<TourApiResponseDTO.TourContentDTO>> partition(
            List<TourApiResponseDTO.TourContentDTO> source,
            int batchSize
    ) {
        List<List<TourApiResponseDTO.TourContentDTO>> result = new ArrayList<>();
        for (int i = 0; i < source.size(); i += batchSize) {
            result.add(source.subList(i, Math.min(i + batchSize, source.size())));
        }
        return result;
    }
}

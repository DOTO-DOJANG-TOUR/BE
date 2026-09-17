package com.doto.global.health;

import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import com.doto.domain.stamp.repository.FestivalVisitRepository;
import com.doto.domain.stamp.repository.StampTourRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 자주 쓰이는 쿼리를 존재하지 않는 값으로 한 번 실행해서, 캐시를 앱 시작 직후 warm 상태로 전환
@Slf4j
@Component
@RequiredArgsConstructor
public class WarmupRunner {

    private final WarmupHealthIndicator warmupHealthIndicator;
    private final FestivalRepository festivalRepository;
    private final StampTourRepository stampTourRepository;
    private final FestivalVisitRepository festivalVisitRepository;
    private final TourSpotRepository tourSpotRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        try {
            festivalRepository.findById(-1L);
            stampTourRepository.existsByMember_IdAndFestival_IdAndStatus(-1L, -1L, StampTourStatus.PROGRESS);
            festivalVisitRepository.findByMember_IdAndStatus(-1L, FestivalVisitStatus.VISITING);
            stampTourRepository.existsByRewardCode("000000");
            tourSpotRepository.existsWithin300Meters(-1L, BigDecimal.ZERO, BigDecimal.ZERO);
        } catch (Exception exception) {
            log.warn("워밍업 쿼리 실행 중 오류가 발생했지만 기동은 계속 진행합니다.", exception);
        } finally {
            warmupHealthIndicator.markWarmedUp();
        }
    }
}

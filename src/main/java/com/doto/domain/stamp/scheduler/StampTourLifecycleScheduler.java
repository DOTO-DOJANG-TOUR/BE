package com.doto.domain.stamp.scheduler;

import com.doto.domain.stamp.service.StampTourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StampTourLifecycleScheduler {

    private final StampTourService stampTourService;

    // 매일 새벽 종료된 축제의 진행 중 스탬프 투어/방문 기록을 청크 단위로 정리
    @Scheduled(cron = "0 10 0 * * *")
    public void closeEndedFestivalTours() {
        while (stampTourService.closeEndedFestivalToursChunk()) {
            // 남은 투어/방문 기록이 없을 때까지 한 청크씩 반복 처리
        }
        log.info("종료된 축제의 스탬프 투어 정리 완료");
    }
}

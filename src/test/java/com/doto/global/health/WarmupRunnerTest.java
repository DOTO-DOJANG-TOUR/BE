package com.doto.global.health;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import com.doto.domain.stamp.repository.FestivalVisitRepository;
import com.doto.domain.stamp.repository.StampTourRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WarmupRunner 단위 테스트")
class WarmupRunnerTest {

    @Mock
    private WarmupHealthIndicator warmupHealthIndicator;
    @Mock
    private FestivalRepository festivalRepository;
    @Mock
    private StampTourRepository stampTourRepository;
    @Mock
    private FestivalVisitRepository festivalVisitRepository;
    @Mock
    private TourSpotRepository tourSpotRepository;

    @Test
    @DisplayName("워밍업 쿼리를 모두 실행하고 헬스 인디케이터를 완료 처리한다")
    void warmsUpAndMarksHealthy() {
        WarmupRunner warmupRunner = new WarmupRunner(
                warmupHealthIndicator, festivalRepository, stampTourRepository,
                festivalVisitRepository, tourSpotRepository
        );

        warmupRunner.warmUp();

        then(festivalRepository).should().findById(-1L);
        then(stampTourRepository).should()
                .existsByMember_IdAndFestival_IdAndStatus(-1L, -1L, StampTourStatus.PROGRESS);
        then(festivalVisitRepository).should().findByMember_IdAndStatus(-1L, FestivalVisitStatus.VISITING);
        then(stampTourRepository).should().existsByRewardCode("000000");
        then(tourSpotRepository).should().existsWithin300Meters(any(), any(), any());
        then(warmupHealthIndicator).should().markWarmedUp();
    }

    @Test
    @DisplayName("워밍업 쿼리 중 예외가 발생해도 헬스 인디케이터는 완료 처리한다")
    void marksHealthyEvenWhenWarmupQueryFails() {
        given(festivalRepository.findById(anyLong())).willThrow(new RuntimeException("db down"));
        WarmupRunner warmupRunner = new WarmupRunner(
                warmupHealthIndicator, festivalRepository, stampTourRepository,
                festivalVisitRepository, tourSpotRepository
        );

        warmupRunner.warmUp();

        then(warmupHealthIndicator).should().markWarmedUp();
        then(stampTourRepository).should(org.mockito.Mockito.never())
                .existsByRewardCode(anyString());
    }
}

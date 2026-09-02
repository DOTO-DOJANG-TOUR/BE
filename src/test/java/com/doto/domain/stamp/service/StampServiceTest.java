package com.doto.domain.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.doto.domain.member.entity.Member;
import com.doto.domain.festival.entity.Festival;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.stamp.entity.TourSpotVisit;
import com.doto.domain.stamp.entity.Stamp;
import com.doto.domain.stamp.dto.StampLocationRequestDTO;
import com.doto.domain.stamp.entity.enums.StampStatus;
import com.doto.domain.stamp.entity.enums.TourSpotVisitStatus;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import com.doto.domain.stamp.exception.StampErrorCode;
import com.doto.domain.stamp.exception.StampException;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.domain.stamp.exception.StampTourException;
import com.doto.domain.stamp.exception.TourSpotVisitErrorCode;
import com.doto.domain.stamp.exception.TourSpotVisitException;
import com.doto.domain.stamp.repository.FestivalVisitRepository;
import com.doto.domain.stamp.repository.StampRepository;
import com.doto.domain.stamp.repository.StampTourRepository;
import com.doto.domain.stamp.repository.TourSpotVisitRepository;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import com.doto.fixture.MemberFixture;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("StampService 단위 테스트")
class StampServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private StampTourRepository stampTourRepository;
    @Mock private StampRepository stampRepository;
    @Mock private TourSpotRepository tourSpotRepository;
    @Mock private FestivalTourSpotRepository festivalTourSpotRepository;
    @Mock private TourSpotVisitRepository tourSpotVisitRepository;
    @Mock private FestivalVisitRepository festivalVisitRepository;

    @InjectMocks private StampService stampService;

    @Nested
    @DisplayName("관광지 방문 시작")
    class StartTourSpotVisit {

        @Test
        @DisplayName("만료된 방문은 EXPIRED로 전환하고 새 방문을 생성한다")
        void expiresPreviousVisitAndCreatesNewVisit() {
            Long memberId = 1L;
            Long tourSpotId = 10L;
            Member member = MemberFixture.create(memberId);
            Festival festival = org.mockito.Mockito.mock(Festival.class);
            TourSpot previousTourSpot = org.mockito.Mockito.mock(TourSpot.class);
            TourSpotVisit expiredVisit = TourSpotVisit.start(member, festival, previousTourSpot, Instant.now().plusSeconds(1));
            ReflectionTestUtils.setField(expiredVisit, "expiresAt", Instant.now().minusSeconds(1));
            TourSpot tourSpot = org.mockito.Mockito.mock(TourSpot.class);
            given(tourSpot.getId()).willReturn(tourSpotId);
            given(tourSpot.getTitle()).willReturn("관광지");
            given(tourSpotVisitRepository.findByMemberIdAndStatusForUpdate(memberId, TourSpotVisitStatus.VISITING))
                    .willReturn(Optional.of(expiredVisit));
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(100L, tourSpotId)).willReturn(true);
            var stampTour = stampTourWithFestival(festival);
            given(stampTourRepository.findByMember_IdAndFestival_IdAndStatus(
                    memberId, 100L, StampTourStatus.PROGRESS
            )).willReturn(Optional.of(stampTour));
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(tourSpotRepository.findById(tourSpotId)).willReturn(Optional.of(tourSpot));

            var response = stampService.startTourSpotVisit(memberId, 100L, tourSpotId);

            assertThat(expiredVisit.getStatus()).isEqualTo(TourSpotVisitStatus.EXPIRED);
            assertThat(response.expiresAt()).isAfter(Instant.now().plus(Duration.ofHours(6)));
            then(tourSpotVisitRepository).should().save(org.mockito.ArgumentMatchers.argThat(visit ->
                    visit.getMember() == member
                            && visit.getTourSpot() == tourSpot
                            && visit.getStatus() == TourSpotVisitStatus.VISITING
            ));
        }

        @Test
        @DisplayName("유효한 방문이 있으면 새 방문을 만들지 않고 충돌 예외를 던진다")
        void throwsConflictWhenActiveVisitExists() {
            Long memberId = 1L;
            TourSpotVisit activeVisit = TourSpotVisit.start(
                    MemberFixture.create(memberId),
                    org.mockito.Mockito.mock(Festival.class),
                    org.mockito.Mockito.mock(TourSpot.class),
                    Instant.now().plusSeconds(60)
            );
            given(tourSpotVisitRepository.findByMemberIdAndStatusForUpdate(memberId, TourSpotVisitStatus.VISITING))
                    .willReturn(Optional.of(activeVisit));
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(100L, 10L)).willReturn(true);
            var stampTour = org.mockito.Mockito.mock(com.doto.domain.stamp.entity.StampTour.class);
            given(stampTourRepository.findByMember_IdAndFestival_IdAndStatus(
                    memberId, 100L, StampTourStatus.PROGRESS
            )).willReturn(Optional.of(stampTour));

            assertThatThrownBy(() -> stampService.startTourSpotVisit(memberId, 100L, 10L))
                    .isInstanceOf(TourSpotVisitException.class)
                    .satisfies(exception -> assertThat(((TourSpotVisitException) exception).getErrorCode())
                            .isEqualTo(TourSpotVisitErrorCode.ACTIVE_VISIT_EXISTS));

            then(tourSpotVisitRepository).should().findByMemberIdAndStatusForUpdate(
                    memberId, TourSpotVisitStatus.VISITING
            );
            then(tourSpotVisitRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("축제 소속 관광지가 아니면 방문을 시작하지 않고 예외를 던진다")
        void throwsNotInFestivalWhenTourSpotDoesNotBelongToFestival() {
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(100L, 10L)).willReturn(false);

            assertThatThrownBy(() -> stampService.startTourSpotVisit(1L, 100L, 10L))
                    .isInstanceOf(StampException.class)
                    .satisfies(exception -> assertThat(((StampException) exception).getErrorCode())
                            .isEqualTo(StampErrorCode.TOUR_SPOT_NOT_IN_FESTIVAL));

            then(stampTourRepository).shouldHaveNoInteractions();
            then(tourSpotVisitRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("진행 중인 스탬프 투어가 없으면 방문을 시작하지 않고 예외를 던진다")
        void throwsNotFoundWhenActiveStampTourDoesNotExist() {
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(100L, 10L)).willReturn(true);
            given(stampTourRepository.findByMember_IdAndFestival_IdAndStatus(
                    1L, 100L, StampTourStatus.PROGRESS
            )).willReturn(Optional.empty());

            assertThatThrownBy(() -> stampService.startTourSpotVisit(1L, 100L, 10L))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_NOT_FOUND));

            then(tourSpotVisitRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관광지 방문 중단")
    class StopTourSpotVisit {

        @Test
        @DisplayName("축제 소속의 활성 방문을 ENDED 상태로 전환한다")
        void endsActiveVisit() {
            Long memberId = 1L;
            Long festivalId = 100L;
            Long tourSpotId = 10L;
            TourSpot tourSpot = org.mockito.Mockito.mock(TourSpot.class);
            TourSpotVisit activeVisit = TourSpotVisit.start(
                    MemberFixture.create(memberId), org.mockito.Mockito.mock(Festival.class), tourSpot, Instant.now().plusSeconds(60)
            );
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(festivalId, tourSpotId))
                    .willReturn(true);
            given(tourSpotVisitRepository.findActiveByMemberIdAndFestivalIdAndTourSpotId(
                    org.mockito.ArgumentMatchers.eq(memberId),
                    org.mockito.ArgumentMatchers.eq(festivalId),
                    org.mockito.ArgumentMatchers.eq(tourSpotId),
                    org.mockito.ArgumentMatchers.eq(TourSpotVisitStatus.VISITING),
                    org.mockito.ArgumentMatchers.any(Instant.class)
            )).willReturn(Optional.of(activeVisit));

            stampService.stopTourSpotVisit(memberId, festivalId, tourSpotId);

            assertThat(activeVisit.getStatus()).isEqualTo(TourSpotVisitStatus.ENDED);
            assertThat(activeVisit.getEndedAt()).isNotNull();
            then(tourSpotVisitRepository).should(org.mockito.Mockito.never())
                    .delete(org.mockito.ArgumentMatchers.any(TourSpotVisit.class));
        }

        @Test
        @DisplayName("축제 소속 관광지가 아니면 방문을 중단하지 않고 예외를 던진다")
        void throwsNotInFestivalWhenTourSpotDoesNotBelongToFestival() {
            given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(100L, 10L)).willReturn(false);

            assertThatThrownBy(() -> stampService.stopTourSpotVisit(1L, 100L, 10L))
                    .isInstanceOf(StampException.class)
                    .satisfies(exception -> assertThat(((StampException) exception).getErrorCode())
                            .isEqualTo(StampErrorCode.TOUR_SPOT_NOT_IN_FESTIVAL));

            then(tourSpotVisitRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관광지 도장 완료")
    class CompleteStamp {

        @Test
        @DisplayName("활성 방문이 없으면 도장을 완료하지 않고 예외를 던진다")
        void throwsNotFoundWhenActiveVisitDoesNotExist() {
            Stamp stamp = stubCompletePreconditions();
            given(tourSpotVisitRepository.findActiveByMemberIdAndFestivalIdAndTourSpotId(
                    org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(100L),
                    org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq(TourSpotVisitStatus.VISITING),
                    org.mockito.ArgumentMatchers.any(Instant.class)
            )).willReturn(Optional.empty());

            assertThatThrownBy(() -> stampService.completeStamp(1L, 100L, 10L, locationRequest()))
                    .isInstanceOf(TourSpotVisitException.class)
                    .satisfies(exception -> assertThat(((TourSpotVisitException) exception).getErrorCode())
                            .isEqualTo(TourSpotVisitErrorCode.ACTIVE_VISIT_NOT_FOUND));

            then(stamp).should(org.mockito.Mockito.never()).complete();
        }

        @Test
        @DisplayName("방문이 재확인 시점에 만료됐으면 도장을 완료하지 않는다")
        void throwsNotFoundWhenVisitExpiresBeforeCompletion() {
            Stamp stamp = stubCompletePreconditions();
            TourSpotVisit expiredVisit = TourSpotVisit.start(
                    MemberFixture.create(1L), org.mockito.Mockito.mock(Festival.class),
                    org.mockito.Mockito.mock(TourSpot.class), Instant.now().plusSeconds(1)
            );
            ReflectionTestUtils.setField(expiredVisit, "expiresAt", Instant.now().minusSeconds(1));
            given(tourSpotVisitRepository.findActiveByMemberIdAndFestivalIdAndTourSpotId(
                    org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(100L),
                    org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq(TourSpotVisitStatus.VISITING),
                    org.mockito.ArgumentMatchers.any(Instant.class)
            )).willReturn(Optional.of(expiredVisit));

            assertThatThrownBy(() -> stampService.completeStamp(1L, 100L, 10L, locationRequest()))
                    .isInstanceOf(TourSpotVisitException.class);

            then(stamp).should(org.mockito.Mockito.never()).complete();
        }

        @Test
        @DisplayName("유효한 방문이면 방문을 종료하고 도장을 완료한다")
        void endsVisitAndCompletesStamp() {
            Stamp stamp = stubCompletePreconditions();
            TourSpotVisit activeVisit = TourSpotVisit.start(
                    MemberFixture.create(1L), org.mockito.Mockito.mock(Festival.class),
                    org.mockito.Mockito.mock(TourSpot.class), Instant.now().plusSeconds(60)
            );
            given(tourSpotVisitRepository.findActiveByMemberIdAndFestivalIdAndTourSpotId(
                    org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(100L),
                    org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq(TourSpotVisitStatus.VISITING),
                    org.mockito.ArgumentMatchers.any(Instant.class)
            )).willReturn(Optional.of(activeVisit));

            stampService.completeStamp(1L, 100L, 10L, locationRequest());

            assertThat(activeVisit.getStatus()).isEqualTo(TourSpotVisitStatus.ENDED);
            then(stamp).should().complete();
        }
    }

    private Stamp stubCompletePreconditions() {
        var stampTour = org.mockito.Mockito.mock(com.doto.domain.stamp.entity.StampTour.class);
        Stamp stamp = org.mockito.Mockito.mock(Stamp.class);
        given(stamp.getStatus()).willReturn(StampStatus.VISITING);
        given(stampTourRepository.findByMemberIdAndFestivalIdAndStatusForUpdate(
                1L, 100L, StampTourStatus.PROGRESS
        )).willReturn(Optional.of(stampTour));
        given(festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(100L, 10L)).willReturn(true);
        given(stampRepository.findByStampTour_IdAndTourSpot_Id(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(10L)))
                .willReturn(Optional.of(stamp));
        given(tourSpotRepository.existsWithin300Meters(
                org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()
        )).willReturn(true);
        return stamp;
    }

    private StampLocationRequestDTO locationRequest() {
        return new StampLocationRequestDTO(BigDecimal.valueOf(126.5), BigDecimal.valueOf(36.3));
    }

    private com.doto.domain.stamp.entity.StampTour stampTourWithFestival(Festival festival) {
        com.doto.domain.stamp.entity.StampTour stampTour = org.mockito.Mockito.mock(com.doto.domain.stamp.entity.StampTour.class);
        given(stampTour.getFestival()).willReturn(festival);
        return stampTour;
    }
}

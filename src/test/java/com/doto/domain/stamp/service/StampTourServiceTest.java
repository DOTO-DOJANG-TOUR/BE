package com.doto.domain.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.stamp.entity.FestivalVisit;
import com.doto.domain.stamp.entity.StampTour;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.domain.stamp.exception.StampTourException;
import com.doto.domain.stamp.repository.FestivalVisitRepository;
import com.doto.domain.stamp.repository.StampTourRepository;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.fixture.FestivalFixture;
import com.doto.fixture.FestivalVisitFixture;
import com.doto.fixture.MemberFixture;
import com.doto.fixture.StampTourFixture;
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
@DisplayName("StampTourService 단위 테스트")
class StampTourServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StampTourRepository stampTourRepository;

    @Mock
    private FestivalVisitRepository festivalVisitRepository;

    @Mock
    private FestivalTourSpotRepository festivalTourSpotRepository;

    @InjectMocks
    private StampTourService stampTourService;

    @Nested
    @DisplayName("스탬프 투어 시작")
    class StartStampTour {

        @Test
        @DisplayName("진행 중인 투어와 방문이 없으면 투어와 방문을 함께 생성한다")
        void createsStampTourAndFestivalVisit() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Festival festival = festivalWithId(festivalId);
            Member member = MemberFixture.create(memberId);
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(festival));
            given(stampTourRepository.existsByMember_IdAndFestival_IdAndStatus(
                    memberId, festivalId, StampTourStatus.PROGRESS
            )).willReturn(false);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(festivalVisitRepository.findByMember_IdAndStatus(memberId, FestivalVisitStatus.VISITING))
                    .willReturn(Optional.empty());

            stampTourService.startStampTour(memberId, festivalId);

            then(festivalVisitRepository).should().save(org.mockito.ArgumentMatchers.argThat(festivalVisit ->
                    festivalVisit.getMember() == member
                            && festivalVisit.getFestival() == festival
                            && festivalVisit.getStatus() == FestivalVisitStatus.VISITING
            ));
            then(stampTourRepository).should().save(org.mockito.ArgumentMatchers.argThat(stampTour ->
                    stampTour.getMember() == member
                            && stampTour.getFestival() == festival
                            && stampTour.getStatus() == StampTourStatus.PROGRESS
            ));
        }

        @Test
        @DisplayName("같은 축제에 진행 중인 투어가 있으면 생성하지 않고 409 예외를 던진다")
        void throwsConflictWhenActiveStampTourExists() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Festival festival = festivalWithId(festivalId);
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(festival));
            given(stampTourRepository.existsByMember_IdAndFestival_IdAndStatus(
                    memberId, festivalId, StampTourStatus.PROGRESS
            )).willReturn(true);

            assertThatThrownBy(() -> stampTourService.startStampTour(memberId, festivalId))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.ACTIVE_STAMP_TOUR_EXISTS));

            then(festivalVisitRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
            then(stampTourRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("다른 축제 방문이 진행 중이면 생성하지 않고 409 예외를 던진다")
        void throwsConflictWhenActiveFestivalVisitExists() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Festival festival = festivalWithId(festivalId);
            Member member = MemberFixture.create(memberId);
            FestivalVisit activeVisit = FestivalVisitFixture.create(member, FestivalFixture.create());
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(festival));
            given(stampTourRepository.existsByMember_IdAndFestival_IdAndStatus(
                    memberId, festivalId, StampTourStatus.PROGRESS
            )).willReturn(false);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(festivalVisitRepository.findByMember_IdAndStatus(memberId, FestivalVisitStatus.VISITING))
                    .willReturn(Optional.of(activeVisit));

            assertThatThrownBy(() -> stampTourService.startStampTour(memberId, festivalId))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.ACTIVE_FESTIVAL_VISIT_EXISTS));

            then(festivalVisitRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
            then(stampTourRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("스탬프 투어 중단")
    class EndStampTour {

        @Test
        @DisplayName("진행 중인 투어가 없으면 STAMP_TOUR_NOT_FOUND 예외를 던진다")
        void throwsNotFoundWhenActiveStampTourDoesNotExist() {
            given(stampTourRepository.findByMember_IdAndFestival_IdAndStatus(1L, 10L, StampTourStatus.PROGRESS))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> stampTourService.endStampTour(1L, 10L))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_NOT_FOUND));
        }

        @Test
        @DisplayName("진행 중인 투어를 삭제하고 활성 축제 방문을 종료한다")
        void deletesStampTourAndEndsFestivalVisit() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Festival festival = festivalWithId(festivalId);
            Member member = MemberFixture.create(memberId);
            StampTour stampTour = StampTourFixture.create(member, festival);
            FestivalVisit festivalVisit = FestivalVisitFixture.create(member, festival);
            given(stampTourRepository.findByMember_IdAndFestival_IdAndStatus(memberId, festivalId, StampTourStatus.PROGRESS))
                    .willReturn(Optional.of(stampTour));
            given(festivalVisitRepository.findByMember_IdAndFestival_IdAndStatus(
                    memberId, festivalId, FestivalVisitStatus.VISITING
            )).willReturn(Optional.of(festivalVisit));

            stampTourService.endStampTour(memberId, festivalId);

            then(stampTourRepository).should().delete(stampTour);
            assertThat(festivalVisit.getStatus()).isEqualTo(FestivalVisitStatus.ENDED);
            assertThat(festivalVisit.getEndedAt()).isNotNull();
        }
    }

    private Festival festivalWithId(Long id) {
        Festival festival = FestivalFixture.create();
        ReflectionTestUtils.setField(festival, "id", id);
        return festival;
    }
}

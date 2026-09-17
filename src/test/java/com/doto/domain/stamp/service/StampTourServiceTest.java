package com.doto.domain.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.stamp.dto.StampTourViewStatus;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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

    @Mock
    private Clock applicationClock;

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
            given(memberRepository.getReferenceById(memberId)).willReturn(member);
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
        @DisplayName("보상 코드가 다른 투어와 겹치면 겹치지 않을 때까지 재발급한 뒤 저장한다")
        void regeneratesRewardCodeUntilUnique() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Festival festival = festivalWithId(festivalId);
            Member member = MemberFixture.create(memberId);
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(festival));
            given(stampTourRepository.existsByMember_IdAndFestival_IdAndStatus(
                    memberId, festivalId, StampTourStatus.PROGRESS
            )).willReturn(false);
            given(memberRepository.getReferenceById(memberId)).willReturn(member);
            given(festivalVisitRepository.findByMember_IdAndStatus(memberId, FestivalVisitStatus.VISITING))
                    .willReturn(Optional.empty());
            given(stampTourRepository.existsByRewardCode(org.mockito.ArgumentMatchers.anyString()))
                    .willReturn(true, true, false);

            stampTourService.startStampTour(memberId, festivalId);

            then(stampTourRepository).should(org.mockito.Mockito.times(3))
                    .existsByRewardCode(org.mockito.ArgumentMatchers.anyString());
            then(stampTourRepository).should().save(org.mockito.ArgumentMatchers.any());
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
            given(memberRepository.getReferenceById(memberId)).willReturn(member);
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

    @Nested
    @DisplayName("스탬프 투어 상태 조회")
    class GetStampTourStatus {

        @Test
        @DisplayName("존재하지 않는 축제면 FESTIVAL_NOT_FOUND 예외를 던진다")
        void throwsNotFoundWhenFestivalDoesNotExist() {
            Long memberId = 1L;
            Long festivalId = 10L;
            given(festivalRepository.findById(festivalId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> stampTourService.getStampTourStatus(memberId, festivalId))
                    .isInstanceOf(FestivalException.class)
                    .satisfies(exception -> assertThat(((FestivalException) exception).getErrorCode())
                            .isEqualTo(FestivalErrorCode.FESTIVAL_NOT_FOUND));
        }

        @Test
        @DisplayName("축제 종료일이 지났으면 스탬프 투어 상태 조회 없이 FESTIVAL_ENDED를 반환한다")
        void returnsFestivalEndedWhenEventEndDatePassed() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Festival festival = festivalWithId(festivalId);
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(festival));
            given(applicationClock.instant()).willReturn(Instant.parse("2026-08-21T00:00:00Z"));

            StampTourViewStatus result = stampTourService.getStampTourStatus(memberId, festivalId);

            assertThat(result).isEqualTo(StampTourViewStatus.FESTIVAL_ENDED);
            then(festivalVisitRepository).should(never())
                    .findByMember_IdAndStatus(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
            then(stampTourRepository).should(never())
                    .findByMember_IdAndFestival_Id(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
        }

        @Test
        @DisplayName("배치로 종료 처리된 투어는 FESTIVAL_ENDED를 반환한다")
        void returnsFestivalEndedWhenStampTourStatusIsEnded() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Festival festival = festivalWithId(festivalId);
            StampTour stampTour = StampTourFixture.create(MemberFixture.create(memberId), festival);
            stampTour.endByFestivalClosure();
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(festival));
            given(applicationClock.instant()).willReturn(Instant.parse("2026-08-16T00:00:00Z"));
            given(festivalVisitRepository.findByMember_IdAndStatus(memberId, FestivalVisitStatus.VISITING))
                    .willReturn(Optional.empty());
            given(stampTourRepository.findByMember_IdAndFestival_Id(memberId, festivalId))
                    .willReturn(Optional.of(stampTour));

            StampTourViewStatus result = stampTourService.getStampTourStatus(memberId, festivalId);

            assertThat(result).isEqualTo(StampTourViewStatus.FESTIVAL_ENDED);
        }

        @Test
        @DisplayName("다른 축제에 참여 중이면 PARTICIPATING_IN_ANOTHER_TOUR를 반환한다")
        void returnsParticipatingInAnotherTour() {
            Long memberId = 1L;
            Long festivalId = 10L;
            Member member = MemberFixture.create(memberId);
            Festival festival = festivalWithId(festivalId);
            FestivalVisit anotherFestivalVisit = FestivalVisitFixture.create(member, festivalWithId(20L));
            given(festivalRepository.findById(festivalId)).willReturn(Optional.of(festival));
            given(applicationClock.instant()).willReturn(Instant.parse("2026-08-16T00:00:00Z"));
            given(festivalVisitRepository.findByMember_IdAndStatus(memberId, FestivalVisitStatus.VISITING))
                    .willReturn(Optional.of(anotherFestivalVisit));

            StampTourViewStatus result = stampTourService.getStampTourStatus(memberId, festivalId);

            assertThat(result).isEqualTo(StampTourViewStatus.PARTICIPATING_IN_ANOTHER_TOUR);
            then(stampTourRepository).should(never()).findByMember_IdAndFestival_Id(memberId, festivalId);
        }
    }

    @Nested
    @DisplayName("축제 종료 배치")
    class CloseEndedFestivalTours {

        @Test
        @DisplayName("한 청크만큼 진행 중 투어와 방문 기록을 함께 종료 처리한다")
        void closesStampTourAndFestivalVisitChunkTogether() {
            Instant now = Instant.parse("2026-08-21T00:00:00Z");
            Festival festival = festivalWithId(10L);
            Member member = MemberFixture.create(1L);
            StampTour stampTour = StampTourFixture.create(member, festival);
            FestivalVisit festivalVisit = FestivalVisitFixture.create(member, festival);
            given(applicationClock.instant()).willReturn(now);
            given(stampTourRepository.findAllByStatusAndFestival_EventEndDateBefore(
                    org.mockito.ArgumentMatchers.eq(StampTourStatus.PROGRESS),
                    org.mockito.ArgumentMatchers.eq(now),
                    org.mockito.ArgumentMatchers.any()
            )).willReturn(java.util.List.of(stampTour));
            given(festivalVisitRepository.findAllByStatusAndFestival_EventEndDateBefore(
                    org.mockito.ArgumentMatchers.eq(FestivalVisitStatus.VISITING),
                    org.mockito.ArgumentMatchers.eq(now),
                    org.mockito.ArgumentMatchers.any()
            )).willReturn(java.util.List.of(festivalVisit));

            boolean hasMore = stampTourService.closeEndedFestivalToursChunk();

            assertThat(stampTour.getStatus()).isEqualTo(StampTourStatus.ENDED);
            assertThat(festivalVisit.getStatus()).isEqualTo(FestivalVisitStatus.ENDED);
            assertThat(festivalVisit.getEndedAt()).isEqualTo(now);
            assertThat(hasMore).isFalse();
        }

        @Test
        @DisplayName("투어와 방문 기록 중 한쪽이라도 청크가 가득 찼으면 true를 반환한다")
        void returnsTrueWhenEitherChunkIsFull() {
            Instant now = Instant.parse("2026-08-21T00:00:00Z");
            Festival festival = festivalWithId(10L);
            Member member = MemberFixture.create(1L);
            java.util.List<StampTour> fullStampTourChunk = java.util.stream.IntStream.range(0, 500)
                    .mapToObj(i -> StampTourFixture.create(member, festival))
                    .toList();
            given(applicationClock.instant()).willReturn(now);
            given(stampTourRepository.findAllByStatusAndFestival_EventEndDateBefore(
                    org.mockito.ArgumentMatchers.eq(StampTourStatus.PROGRESS),
                    org.mockito.ArgumentMatchers.eq(now),
                    org.mockito.ArgumentMatchers.any()
            )).willReturn(fullStampTourChunk);
            given(festivalVisitRepository.findAllByStatusAndFestival_EventEndDateBefore(
                    org.mockito.ArgumentMatchers.eq(FestivalVisitStatus.VISITING),
                    org.mockito.ArgumentMatchers.eq(now),
                    org.mockito.ArgumentMatchers.any()
            )).willReturn(java.util.List.of());

            boolean hasMore = stampTourService.closeEndedFestivalToursChunk();

            assertThat(hasMore).isTrue();
        }
    }

    @Nested
    @DisplayName("QR코드로 스탬프 투어 보상 미리보기 조회")
    class PreviewStampTourReward {

        @Test
        @DisplayName("완료된 투어면 상태를 바꾸지 않고 회원·투어 정보를 반환한다")
        void returnsPreviewWithoutChangingStatus() {
            String rewardCode = "048213";
            Festival festival = festivalWithId(10L);
            Member member = MemberFixture.create(1L);
            StampTour stampTour = StampTourFixture.create(member, festival);
            stampTour.completeStamp();
            stampTour.completeStamp();
            stampTour.completeStamp();
            given(stampTourRepository.findByRewardCode(rewardCode)).willReturn(Optional.of(stampTour));

            var result = stampTourService.previewStampTourReward(rewardCode);

            assertThat(stampTour.getStatus()).isEqualTo(StampTourStatus.COMPLETED);
            assertThat(result.memberNickname()).isEqualTo(member.getNickname());
            assertThat(result.pinNumber()).isEqualTo(stampTour.getRewardCode());
            assertThat(result.tourName()).isEqualTo(festival.getTitle());
            then(stampTourRepository).should(never()).findByRewardCodeForUpdate(org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("보상 코드에 해당하는 투어가 없으면 STAMP_TOUR_NOT_FOUND 예외를 던진다")
        void throwsNotFoundWhenStampTourDoesNotExist() {
            given(stampTourRepository.findByRewardCode("000000")).willReturn(Optional.empty());

            assertThatThrownBy(() -> stampTourService.previewStampTourReward("000000"))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_NOT_FOUND));
        }

        @Test
        @DisplayName("도장을 모두 완료하지 않았으면 STAMP_TOUR_NOT_COMPLETED 예외를 던진다")
        void throwsConflictWhenStampTourNotCompleted() {
            String rewardCode = "048213";
            StampTour stampTour = StampTourFixture.create(MemberFixture.create(1L), festivalWithId(10L));
            given(stampTourRepository.findByRewardCode(rewardCode)).willReturn(Optional.of(stampTour));

            assertThatThrownBy(() -> stampTourService.previewStampTourReward(rewardCode))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_NOT_COMPLETED));
        }

        @Test
        @DisplayName("이미 보상을 받은 투어면 STAMP_TOUR_ALREADY_REWARDED 예외를 던진다")
        void throwsConflictWhenAlreadyRewarded() {
            String rewardCode = "048213";
            StampTour stampTour = StampTourFixture.create(MemberFixture.create(1L), festivalWithId(10L));
            stampTour.completeStamp();
            stampTour.completeStamp();
            stampTour.completeStamp();
            stampTour.reward();
            given(stampTourRepository.findByRewardCode(rewardCode)).willReturn(Optional.of(stampTour));

            assertThatThrownBy(() -> stampTourService.previewStampTourReward(rewardCode))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_ALREADY_REWARDED));
        }
    }

    @Nested
    @DisplayName("QR코드로 스탬프 투어 보상 처리")
    class RewardStampTourByRewardCode {

        @Test
        @DisplayName("완료된 투어를 보상 처리하고 REWARDED 상태로 변경한다")
        void rewardsCompletedStampTour() {
            String rewardCode = "048213";
            Festival festival = festivalWithId(10L);
            Member member = MemberFixture.create(1L);
            StampTour stampTour = StampTourFixture.create(member, festival);
            stampTour.completeStamp();
            stampTour.completeStamp();
            stampTour.completeStamp();
            given(stampTourRepository.findByRewardCodeForUpdate(rewardCode)).willReturn(Optional.of(stampTour));
            given(applicationClock.instant()).willReturn(Instant.parse("2026-09-18T00:00:00Z"));
            given(applicationClock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));

            var result = stampTourService.rewardStampTourByRewardCode(rewardCode);

            assertThat(stampTour.getStatus()).isEqualTo(StampTourStatus.REWARDED);
            assertThat(result.memberNickname()).isEqualTo(member.getNickname());
            assertThat(result.pinNumber()).isEqualTo(stampTour.getRewardCode());
            assertThat(result.tourName()).isEqualTo(festival.getTitle());
            assertThat(result.rewardedAt()).isEqualTo("2026.09.18 (금)");
        }

        @Test
        @DisplayName("보상 코드에 해당하는 투어가 없으면 STAMP_TOUR_NOT_FOUND 예외를 던진다")
        void throwsNotFoundWhenStampTourDoesNotExist() {
            given(stampTourRepository.findByRewardCodeForUpdate("000000")).willReturn(Optional.empty());

            assertThatThrownBy(() -> stampTourService.rewardStampTourByRewardCode("000000"))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_NOT_FOUND));
        }

        @Test
        @DisplayName("도장을 모두 완료하지 않았으면 STAMP_TOUR_NOT_COMPLETED 예외를 던진다")
        void throwsConflictWhenStampTourNotCompleted() {
            String rewardCode = "048213";
            StampTour stampTour = StampTourFixture.create(MemberFixture.create(1L), festivalWithId(10L));
            given(stampTourRepository.findByRewardCodeForUpdate(rewardCode)).willReturn(Optional.of(stampTour));

            assertThatThrownBy(() -> stampTourService.rewardStampTourByRewardCode(rewardCode))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_NOT_COMPLETED));
        }

        @Test
        @DisplayName("이미 보상을 받은 투어면 STAMP_TOUR_ALREADY_REWARDED 예외를 던진다")
        void throwsConflictWhenAlreadyRewarded() {
            String rewardCode = "048213";
            StampTour stampTour = StampTourFixture.create(MemberFixture.create(1L), festivalWithId(10L));
            stampTour.completeStamp();
            stampTour.completeStamp();
            stampTour.completeStamp();
            stampTour.reward();
            given(stampTourRepository.findByRewardCodeForUpdate(rewardCode)).willReturn(Optional.of(stampTour));

            assertThatThrownBy(() -> stampTourService.rewardStampTourByRewardCode(rewardCode))
                    .isInstanceOf(StampTourException.class)
                    .satisfies(exception -> assertThat(((StampTourException) exception).getErrorCode())
                            .isEqualTo(StampTourErrorCode.STAMP_TOUR_ALREADY_REWARDED));
        }
    }

    private Festival festivalWithId(Long id) {
        Festival festival = FestivalFixture.create();
        ReflectionTestUtils.setField(festival, "id", id);
        return festival;
    }
}

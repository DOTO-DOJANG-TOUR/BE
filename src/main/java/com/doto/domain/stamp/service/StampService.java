package com.doto.domain.stamp.service;

import com.doto.domain.member.entity.Member;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.domain.member.exception.MemberException;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.stamp.dto.CurrentVisitTourSpotResponseDTO;
import com.doto.domain.stamp.dto.StampLocationRequestDTO;
import com.doto.domain.stamp.dto.StampResponseDTO;
import com.doto.domain.stamp.entity.Stamp;
import com.doto.domain.stamp.entity.StampTour;
import com.doto.domain.stamp.entity.TourSpotVisit;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import com.doto.domain.stamp.entity.enums.StampStatus;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import com.doto.domain.stamp.entity.enums.TourSpotVisitStatus;
import com.doto.domain.stamp.exception.StampErrorCode;
import com.doto.domain.stamp.exception.StampException;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.domain.stamp.exception.StampTourException;
import com.doto.domain.stamp.exception.TourSpotVisitErrorCode;
import com.doto.domain.stamp.exception.TourSpotVisitException;
import com.doto.domain.stamp.repository.StampRepository;
import com.doto.domain.stamp.repository.StampTourRepository;
import com.doto.domain.stamp.repository.TourSpotVisitRepository;
import com.doto.domain.stamp.repository.FestivalVisitRepository;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampService {

    private static final Duration TOUR_SPOT_VISIT_DURATION = Duration.ofHours(7);

    private final MemberRepository memberRepository;
    private final StampTourRepository stampTourRepository;
    private final StampRepository stampRepository;
    private final TourSpotRepository tourSpotRepository;
    private final FestivalTourSpotRepository festivalTourSpotRepository;
    private final TourSpotVisitRepository tourSpotVisitRepository;
    private final FestivalVisitRepository festivalVisitRepository;

    // 관광지 도장 찍기 시작
    @Transactional
    public CurrentVisitTourSpotResponseDTO startTourSpotVisit(
            Long memberId,
            Long festivalId,
            Long tourSpotId
    ) {
        if (!festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(festivalId, tourSpotId)) {
            throw new StampException(StampErrorCode.TOUR_SPOT_NOT_IN_FESTIVAL);
        }
        StampTour stampTour = stampTourRepository.findByMember_IdAndFestival_IdAndStatus(
                        memberId, festivalId, StampTourStatus.PROGRESS
                )
                .orElseThrow(() -> new StampTourException(StampTourErrorCode.STAMP_TOUR_NOT_FOUND));

        Instant now = Instant.now();
        tourSpotVisitRepository.findByMemberIdAndStatusForUpdate(memberId, TourSpotVisitStatus.VISITING)
                .ifPresent(visit -> {
                    if (!visit.isExpiredAt(now)) {
                        throw new TourSpotVisitException(TourSpotVisitErrorCode.ACTIVE_VISIT_EXISTS);
                    }
                    visit.expire(now);
                });

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        TourSpot tourSpot = tourSpotRepository.findById(tourSpotId)
                .orElseThrow(() -> new TourSpotVisitException(TourSpotVisitErrorCode.TOUR_SPOT_NOT_FOUND));
        TourSpotVisit visit = TourSpotVisit.start(member, stampTour.getFestival(), tourSpot, now.plus(TOUR_SPOT_VISIT_DURATION));
        tourSpotVisitRepository.save(visit);

        return new CurrentVisitTourSpotResponseDTO(
                String.valueOf(tourSpot.getId()),
                tourSpot.getTitle(),
                visit.getExpiresAt()
        );
    }

    // 관광지 도장 찍기 중단
    @Transactional
    public void stopTourSpotVisit(
            Long memberId,
            Long festivalId,
            Long tourSpotId
    ) {
        if (!festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(festivalId, tourSpotId)) {
            throw new StampException(StampErrorCode.TOUR_SPOT_NOT_IN_FESTIVAL);
        }

        // 활성 방문 중단처리
        Instant now = Instant.now();
        TourSpotVisit tourSpotVisit = tourSpotVisitRepository.findActiveByMemberIdAndFestivalIdAndTourSpotId(
                        memberId, festivalId, tourSpotId, TourSpotVisitStatus.VISITING, now
                )
                .orElseThrow(() -> new TourSpotVisitException(TourSpotVisitErrorCode.ACTIVE_VISIT_NOT_FOUND));
        tourSpotVisit.end(now);
    }


    // 관광지 도장 완료 처리
    @Transactional
    public StampResponseDTO completeStamp(
            Long memberId,
            Long festivalId,
            Long tourSpotId,
            StampLocationRequestDTO locationRequest
    ) {
        StampTour stampTour = stampTourRepository.findByMemberIdAndFestivalIdAndStatusForUpdate(
                        memberId,
                        festivalId,
                        StampTourStatus.PROGRESS
                )
                .orElseThrow(() -> new StampTourException(StampTourErrorCode.STAMP_TOUR_NOT_FOUND));

        if (!festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(festivalId, tourSpotId)) {
            throw new StampException(StampErrorCode.TOUR_SPOT_NOT_IN_FESTIVAL);
        }

        Stamp stamp = stampRepository.findByStampTour_IdAndTourSpot_Id(stampTour.getId(), tourSpotId)
                .orElseGet(() -> Stamp.create(stampTour, findTourSpot(tourSpotId)));
        if (stamp.getStatus() == StampStatus.COMPLETED) {
            throw new StampException(StampErrorCode.STAMP_ALREADY_COMPLETED);
        }

        // 300m 이내인지 확인
        if (!tourSpotRepository.existsWithin300Meters(
                tourSpotId,
                locationRequest.mapX(),
                locationRequest.mapY()
        )) {
            throw new StampException(StampErrorCode.TOUR_SPOT_OUT_OF_RANGE);
        }

        // 활성 방문 완료처리
        Instant visitCheckedAt = Instant.now();
        TourSpotVisit tourSpotVisit = tourSpotVisitRepository.findActiveByMemberIdAndFestivalIdAndTourSpotId(
                        memberId, festivalId, tourSpotId, TourSpotVisitStatus.VISITING, visitCheckedAt
                )
                .orElseThrow(() -> new TourSpotVisitException(TourSpotVisitErrorCode.ACTIVE_VISIT_NOT_FOUND));
        Instant completedAt = Instant.now();
        if (tourSpotVisit.isExpiredAt(completedAt)) {
            throw new TourSpotVisitException(TourSpotVisitErrorCode.ACTIVE_VISIT_NOT_FOUND);
        }
        tourSpotVisit.end(completedAt);

        stamp.complete();
        stampTour.completeStamp();
        if (stampTour.getStatus() == StampTourStatus.COMPLETED) {
            festivalVisitRepository.findByMember_IdAndFestival_IdAndStatus(
                            memberId,
                            festivalId,
                            FestivalVisitStatus.VISITING
                    )
                    .ifPresent(festivalVisit -> festivalVisit.end(Instant.now()));
        }
        stampRepository.save(stamp);
        return new StampResponseDTO(String.valueOf(stamp.getId()));
    }

    // 현재 방문중인 관광지 조회, 없으면 null
    public CurrentVisitTourSpotResponseDTO getCurrentVisitTourSpot(Long memberId) {
        return tourSpotVisitRepository.findActiveByMemberId(memberId, TourSpotVisitStatus.VISITING, Instant.now())
                .map(tourSpotVisit -> new CurrentVisitTourSpotResponseDTO(
                        String.valueOf(tourSpotVisit.getTourSpot().getId()),
                        tourSpotVisit.getTourSpot().getTitle(),
                        tourSpotVisit.getExpiresAt()
                ))
                .orElse(null);
    }


    private TourSpot findTourSpot(Long tourSpotId) {
        return tourSpotRepository.findById(tourSpotId)
                .orElseThrow(() -> new StampException(StampErrorCode.TOUR_SPOT_NOT_IN_FESTIVAL));
    }
}

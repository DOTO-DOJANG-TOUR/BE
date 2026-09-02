package com.doto.domain.stamp.service;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import com.doto.domain.festival.repository.FestivalRepository;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.domain.member.exception.MemberException;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.stamp.dto.StampTourDetailResponseDTO;
import com.doto.domain.stamp.dto.StampTourSpotItemResponseDTO;
import com.doto.domain.stamp.dto.StampTourViewStatus;
import com.doto.domain.stamp.entity.FestivalVisit;
import com.doto.domain.stamp.entity.StampTour;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.domain.stamp.exception.StampTourException;
import com.doto.domain.stamp.repository.FestivalVisitRepository;
import com.doto.domain.stamp.repository.StampTourRepository;
import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.entity.enums.TourSpotCategoryFilter;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.global.util.DistanceUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampTourService {
    private final FestivalRepository festivalRepository;
    private final MemberRepository memberRepository;
    private final StampTourRepository stampTourRepository;
    private final FestivalVisitRepository festivalVisitRepository;
    private final FestivalTourSpotRepository festivalTourSpotRepository;

    // 스탬프 투어 시작
    @Transactional
    public void startStampTour(Long memberId, Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new FestivalException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        boolean hasActiveStampTour = stampTourRepository.existsByMember_IdAndFestival_IdAndStatus(
                memberId, festivalId, StampTourStatus.PROGRESS);
        if (hasActiveStampTour) {
            throw new StampTourException(StampTourErrorCode.ACTIVE_STAMP_TOUR_EXISTS);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (festivalVisitRepository.findByMember_IdAndStatus(memberId, FestivalVisitStatus.VISITING).isPresent()) {
            throw new StampTourException(StampTourErrorCode.ACTIVE_FESTIVAL_VISIT_EXISTS);
        }

        festivalVisitRepository.save(FestivalVisit.start(member, festival));
        stampTourRepository.save(StampTour.create(member, festival));
    }

    // 스탬프 투어 중단하기
    @Transactional
    public void endStampTour(Long memberId, Long festivalId) {
        // 멤버와 축제에 해당하는 진행 중인 스탬프 투어 조회
        StampTour stampTour = stampTourRepository.findByMember_IdAndFestival_IdAndStatus(
                        memberId, festivalId, StampTourStatus.PROGRESS)
                // 없으면 예외 발생
                .orElseThrow(() -> new StampTourException(StampTourErrorCode.STAMP_TOUR_NOT_FOUND));

        stampTourRepository.delete(stampTour);
        festivalVisitRepository.findByMember_IdAndFestival_IdAndStatus(
                        memberId,
                        festivalId,
                        FestivalVisitStatus.VISITING
                )
                .ifPresent(festivalVisit -> festivalVisit.end(java.time.Instant.now()));
    }

    // 스탬프 투어 상태 조회
    public StampTourViewStatus getStampTourStatus(Long memberId, Long festivalId) {
        StampTourStatus status = stampTourRepository.findByMember_IdAndFestival_Id(memberId, festivalId)
                .map(StampTour::getStatus)
                .orElse(null);
        return StampTourViewStatus.from(status);
    }


    // 현재 방문중인 스탬프 투어 조회
    public StampTourDetailResponseDTO getCurrentStampTour(Long memberId, TourSpotCategoryFilter categoryFilter) {
        return festivalVisitRepository.findByMember_IdAndStatus(memberId, FestivalVisitStatus.VISITING)
                .map(festivalVisit -> {
                    List<FestivalTourSpot> festivalTourSpots = festivalTourSpotRepository
                            .findAllWithTourSpotByFestivalIdAndCategory(
                                    festivalVisit.getFestival().getId(),
                                    categoryFilter.categoryNameOrNull()
                            );
                    int stampCount = stampTourRepository
                            .findByMember_IdAndFestival_Id(memberId, festivalVisit.getFestival().getId())
                            .map(StampTour::getCompletedStampCount)
                            .orElse(0);

                    return new StampTourDetailResponseDTO(
                            String.valueOf(festivalVisit.getFestival().getId()),
                            festivalVisit.getFestival().getTitle(),
                            stampCount,
                            festivalTourSpots.stream()
                                    .map(this::toStampTourSpotItem)
                                    .toList()
                    );
                })
                .orElse(null);
    }



    private StampTourSpotItemResponseDTO toStampTourSpotItem(FestivalTourSpot festivalTourSpot) {
        TourSpot tourSpot = festivalTourSpot.getTourSpot();
        return new StampTourSpotItemResponseDTO(
                String.valueOf(tourSpot.getId()),
                tourSpot.getTitle(),
                tourSpot.getImageUrl(),
                tourSpot.getAddress(),
                String.valueOf(tourSpot.getLocation().getX()),
                String.valueOf(tourSpot.getLocation().getY()),
                tourSpot.getCategory(),
                DistanceUtils.format(festivalTourSpot.getDistanceMeters())
        );
    }
}

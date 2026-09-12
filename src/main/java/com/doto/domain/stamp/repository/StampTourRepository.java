package com.doto.domain.stamp.repository;

import com.doto.domain.stamp.entity.StampTour;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StampTourRepository extends JpaRepository<StampTour, Long> {

    boolean existsByMember_IdAndFestival_IdAndStatus(Long memberId, Long festivalId, StampTourStatus status);

    Optional<StampTour> findByMember_IdAndFestival_IdAndStatus(Long memberId, Long festivalId, StampTourStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT stampTour FROM StampTour stampTour "
            + "WHERE stampTour.member.id = :memberId "
            + "AND stampTour.festival.id = :festivalId "
            + "AND stampTour.status = :status")
    Optional<StampTour> findByMemberIdAndFestivalIdAndStatusForUpdate(
            @Param("memberId") Long memberId,
            @Param("festivalId") Long festivalId,
            @Param("status") StampTourStatus status
    );

    Optional<StampTour> findByMember_IdAndFestival_Id(Long memberId, Long festivalId);

    // 신규 투어 생성 시 보상 코드가 다른 투어와 겹치지 않는지 확인
    boolean existsByRewardCode(String rewardCode);

    // QR코드 스캔으로 보상 처리할 때 동시 요청으로 중복 보상되지 않도록 잠금 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT stampTour FROM StampTour stampTour WHERE stampTour.rewardCode = :rewardCode")
    Optional<StampTour> findByRewardCodeForUpdate(@Param("rewardCode") String rewardCode);

    // 회원이 참여한 모든 스탬프 투어 조회 (내 도장 현황용) - 축제마다 하나씩 있어 여러 건일 수 있음
    List<StampTour> findAllByMember_Id(Long memberId);
}

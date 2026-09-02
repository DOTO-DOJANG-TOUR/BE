package com.doto.domain.stamp.repository;

import com.doto.domain.stamp.entity.StampTour;
import com.doto.domain.stamp.entity.enums.StampTourStatus;
import jakarta.persistence.LockModeType;
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
}

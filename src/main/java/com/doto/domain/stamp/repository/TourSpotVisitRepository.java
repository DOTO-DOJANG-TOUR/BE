package com.doto.domain.stamp.repository;

import com.doto.domain.stamp.entity.TourSpotVisit;
import com.doto.domain.stamp.entity.enums.TourSpotVisitStatus;
import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourSpotVisitRepository extends JpaRepository<TourSpotVisit, Long> {

    @Query("SELECT visit FROM TourSpotVisit visit "
            + "WHERE visit.member.id = :memberId "
            + "AND visit.status = :status "
            + "AND visit.expiresAt > :now")
    Optional<TourSpotVisit> findActiveByMemberId(
            @Param("memberId") Long memberId,
            @Param("status") TourSpotVisitStatus status,
            @Param("now") Instant now
    );

    @Query("SELECT visit FROM TourSpotVisit visit "
            + "WHERE visit.member.id = :memberId "
            + "AND visit.festival.id = :festivalId "
            + "AND visit.tourSpot.id = :tourSpotId "
            + "AND visit.status = :status "
            + "AND visit.expiresAt > :now")
    Optional<TourSpotVisit> findActiveByMemberIdAndFestivalIdAndTourSpotId(
            @Param("memberId") Long memberId,
            @Param("festivalId") Long festivalId,
            @Param("tourSpotId") Long tourSpotId,
            @Param("status") TourSpotVisitStatus status,
            @Param("now") Instant now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT visit FROM TourSpotVisit visit "
            + "WHERE visit.member.id = :memberId "
            + "AND visit.status = :status")
    Optional<TourSpotVisit> findByMemberIdAndStatusForUpdate(
            @Param("memberId") Long memberId,
            @Param("status") TourSpotVisitStatus status
    );
}

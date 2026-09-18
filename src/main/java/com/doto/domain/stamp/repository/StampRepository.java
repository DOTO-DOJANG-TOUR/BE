package com.doto.domain.stamp.repository;

import com.doto.domain.stamp.entity.Stamp;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StampRepository extends JpaRepository<Stamp, Long> {

    Optional<Stamp> findByStampTour_IdAndTourSpot_Id(Long stampTourId, Long tourSpotId);

    List<Stamp> findByStampTour_Id(Long stampTourId);

    // Stamp는 completeStamp에서 생성과 동시에 COMPLETED로 저장되는 게 유일한 경로라 존재 자체가 방문 완료를 의미한다
    boolean existsByStampTour_Member_IdAndStampTour_Festival_IdAndTourSpot_Id(
            Long memberId, Long festivalId, Long tourSpotId
    );
}

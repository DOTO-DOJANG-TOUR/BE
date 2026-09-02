package com.doto.domain.stamp.repository;

import com.doto.domain.stamp.entity.FestivalVisit;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalVisitRepository extends JpaRepository<FestivalVisit, Long> {

    Optional<FestivalVisit> findByMember_IdAndStatus(Long memberId, FestivalVisitStatus status);

    Optional<FestivalVisit> findByMember_IdAndFestival_IdAndStatus(
            Long memberId,
            Long festivalId,
            FestivalVisitStatus status
    );
}

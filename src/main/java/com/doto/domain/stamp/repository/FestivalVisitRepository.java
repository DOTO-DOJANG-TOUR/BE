package com.doto.domain.stamp.repository;

import com.doto.domain.stamp.entity.FestivalVisit;
import com.doto.domain.stamp.entity.enums.FestivalVisitStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalVisitRepository extends JpaRepository<FestivalVisit, Long> {

    Optional<FestivalVisit> findByMember_IdAndStatus(Long memberId, FestivalVisitStatus status);

    Optional<FestivalVisit> findByMember_IdAndFestival_IdAndStatus(
            Long memberId,
            Long festivalId,
            FestivalVisitStatus status
    );

    // 축제 종료 배치용 - 축제가 이미 끝났는데 아직 방문 중인 기록을 페이지 단위로 조회
    List<FestivalVisit> findAllByStatusAndFestival_EventEndDateBefore(
            FestivalVisitStatus status, Instant instant, Pageable pageable);
}

package com.doto.domain.festival.repository;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.enums.Region;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    Optional<Festival> findByContentId(Long contentId);

    // 오늘의 축제: eventEndDate-오늘 크기순 = eventEndDate 오름차순과 동일해서 eventEndDate 그대로 정렬/커서에 사용
    @Query("SELECT f FROM Festival f "
            + "WHERE f.eventStartDate <= :now "
            + "AND f.eventEndDate >= :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (f.eventEndDate > :cursorEventEndDate "
            + "     OR (f.eventEndDate = :cursorEventEndDate "
            + "         AND f.id > :cursorId)) "
            + "ORDER BY f.eventEndDate ASC, f.id ASC")
    List<Festival> findTodayFestivals(
            @Param("now") Instant now,
            @Param("cursorEventEndDate") Instant cursorEventEndDate,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 앞으로의 축제: eventStartDate-오늘 크기순 = eventStartDate 오름차순과 동일해서 eventStartDate 그대로 사용
    @Query("SELECT f FROM Festival f "
            + "WHERE f.eventStartDate > :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (f.eventStartDate > :cursorEventStartDate "
            + "     OR (f.eventStartDate = :cursorEventStartDate AND (f.eventEndDate - f.eventStartDate) > :cursorDuration) "
            + "     OR (f.eventStartDate = :cursorEventStartDate AND (f.eventEndDate - f.eventStartDate) = :cursorDuration "
            + "         AND f.id > :cursorId)) "
            + "ORDER BY f.eventStartDate ASC, (f.eventEndDate - f.eventStartDate) ASC, f.id ASC")
    List<Festival> findUpcomingFestivals(
            @Param("now") Instant now,
            @Param("cursorEventStartDate") Instant cursorEventStartDate,
            @Param("cursorDuration") Duration cursorDuration,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 지역별 축제(종료임박순): event_start_date<=오늘<=event_end_date인 진행중 축제만 대상(오늘의 축제와 필터 동일 + 지역 조건)
    @Query("SELECT f FROM Festival f "
            + "WHERE f.legalRegion IN :regions "
            + "AND f.eventStartDate <= :now "
            + "AND f.eventEndDate >= :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (f.eventEndDate > :cursorEventEndDate "
            + "     OR (f.eventEndDate = :cursorEventEndDate "
            + "         AND f.id > :cursorId)) "
            + "ORDER BY f.eventEndDate ASC, f.id ASC")
    List<Festival> findByRegionGroupOrderByEndDate(
            @Param("regions") Set<Region> regions,
            @Param("now") Instant now,
            @Param("cursorEventEndDate") Instant cursorEventEndDate,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 지역별 축제(개최임박순): event_start_date>오늘인 개최 전 축제만 대상(앞으로의 축제와 필터 동일 + 지역 조건)
    @Query("SELECT f FROM Festival f "
            + "WHERE f.legalRegion IN :regions "
            + "AND f.eventStartDate > :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (f.eventStartDate > :cursorEventStartDate "
            + "     OR (f.eventStartDate = :cursorEventStartDate AND (f.eventEndDate - f.eventStartDate) > :cursorDuration) "
            + "     OR (f.eventStartDate = :cursorEventStartDate AND (f.eventEndDate - f.eventStartDate) = :cursorDuration "
            + "         AND f.id > :cursorId)) "
            + "ORDER BY f.eventStartDate ASC, (f.eventEndDate - f.eventStartDate) ASC, f.id ASC")
    List<Festival> findByRegionGroupOrderByStartDate(
            @Param("regions") Set<Region> regions,
            @Param("now") Instant now,
            @Param("cursorEventStartDate") Instant cursorEventStartDate,
            @Param("cursorDuration") Duration cursorDuration,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}

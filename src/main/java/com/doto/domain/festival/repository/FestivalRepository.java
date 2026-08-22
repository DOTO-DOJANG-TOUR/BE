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
    // 첫 페이지는 service에서 EPOCH/"" sentinel 전달
    @Query("SELECT f FROM Festival f "
            + "WHERE f.eventStartDate <= :now "
            + "AND f.eventEndDate >= :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (f.eventEndDate > :cursorEventEndDate "
            + "     OR (f.eventEndDate = :cursorEventEndDate "
            + "         AND function('regexp_replace', f.title, '[^가-힣a-zA-Z0-9]', '', 'g') > :cursorTitle)) "
            + "ORDER BY f.eventEndDate ASC, f.title ASC")
    List<Festival> findTodayFestivals(
            @Param("now") Instant now,
            @Param("cursorEventEndDate") Instant cursorEventEndDate,
            @Param("cursorTitle") String cursorTitle,
            Pageable pageable
    );

    // 앞으로의 축제: eventStartDate-오늘 크기순 = eventStartDate 오름차순과 동일해서 eventStartDate 그대로 사용
    // 첫 페이지는 service에서 EPOCH/-1초/"" sentinel 전달, 시작 전 축제만 대상
    @Query("SELECT f FROM Festival f "
            + "WHERE f.eventStartDate > :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (f.eventStartDate > :cursorEventStartDate "
            + "     OR (f.eventStartDate = :cursorEventStartDate AND (f.eventEndDate - f.eventStartDate) > :cursorDuration) "
            + "     OR (f.eventStartDate = :cursorEventStartDate AND (f.eventEndDate - f.eventStartDate) = :cursorDuration "
            + "         AND function('regexp_replace', f.title, '[^가-힣a-zA-Z0-9]', '', 'g') > :cursorTitle)) "
            + "ORDER BY f.eventStartDate ASC, (f.eventEndDate - f.eventStartDate) ASC, f.title ASC")
    List<Festival> findUpcomingFestivals(
            @Param("now") Instant now,
            @Param("cursorEventStartDate") Instant cursorEventStartDate,
            @Param("cursorDuration") Duration cursorDuration,
            @Param("cursorTitle") String cursorTitle,
            Pageable pageable
    );

    // 지역별 축제(종료임박순): 개최중 전체가 개최전 전체보다 앞, 그 안에서 eventEndDate>eventStartDate>title 순
    // statusRank는 개최중=0/개최전=1, 첫 페이지는 service에서 -1(EPOCH,EPOCH,"") sentinel 전달
    @Query("SELECT f FROM Festival f "
            + "WHERE f.legalRegion IN :regions "
            + "AND f.eventEndDate >= :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND ((CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) > :cursorStatusRank "
            + "     OR ((CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) = :cursorStatusRank AND f.eventEndDate > :cursorEventEndDate) "
            + "     OR ((CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) = :cursorStatusRank AND f.eventEndDate = :cursorEventEndDate "
            + "         AND f.eventStartDate > :cursorEventStartDate) "
            + "     OR ((CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) = :cursorStatusRank AND f.eventEndDate = :cursorEventEndDate "
            + "         AND f.eventStartDate = :cursorEventStartDate "
            + "         AND function('regexp_replace', f.title, '[^가-힣a-zA-Z0-9]', '', 'g') > :cursorTitle)) "
            + "ORDER BY (CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) ASC, "
            + "f.eventEndDate ASC, f.eventStartDate ASC, f.title ASC")
    List<Festival> findByRegionGroupOrderByEndDate(
            @Param("regions") Set<Region> regions,
            @Param("now") Instant now,
            @Param("cursorStatusRank") int cursorStatusRank,
            @Param("cursorEventEndDate") Instant cursorEventEndDate,
            @Param("cursorEventStartDate") Instant cursorEventStartDate,
            @Param("cursorTitle") String cursorTitle,
            Pageable pageable
    );

    // 지역별 축제(개최임박순): 개최중 전체가 개최전 전체보다 앞, 그 안에서 eventStartDate>title 순
    @Query("SELECT f FROM Festival f "
            + "WHERE f.legalRegion IN :regions "
            + "AND f.eventEndDate >= :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND ((CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) > :cursorStatusRank "
            + "     OR ((CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) = :cursorStatusRank AND f.eventStartDate > :cursorEventStartDate) "
            + "     OR ((CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) = :cursorStatusRank AND f.eventStartDate = :cursorEventStartDate "
            + "         AND function('regexp_replace', f.title, '[^가-힣a-zA-Z0-9]', '', 'g') > :cursorTitle)) "
            + "ORDER BY (CASE WHEN f.eventStartDate <= :now THEN 0 ELSE 1 END) ASC, f.eventStartDate ASC, f.title ASC")
    List<Festival> findByRegionGroupOrderByStartDate(
            @Param("regions") Set<Region> regions,
            @Param("now") Instant now,
            @Param("cursorStatusRank") int cursorStatusRank,
            @Param("cursorEventStartDate") Instant cursorEventStartDate,
            @Param("cursorTitle") String cursorTitle,
            Pageable pageable
    );
}

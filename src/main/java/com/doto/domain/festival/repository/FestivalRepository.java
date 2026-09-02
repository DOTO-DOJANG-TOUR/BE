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

    // 통합 검색: 주소/지역/카테고리는 인덱스가 없어 title로만 매칭한다(성능 이유). status는 query에 포함된 진행/오늘·예정/내일 키워드로 서비스에서 판단해 넘겨받는다.
    // title 제목 유사도(pg_trgm similarity) 내림차순으로 정렬. similarity() 계산을 candidates CTE에서 행당 한 번만 하도록 MATERIALIZED로 고정
    // (그냥 두면 옵티마이저가 인라이닝해서 WHERE 절의 커서 분기마다 다시 계산할 수 있음).
    @Query(value = "WITH candidates AS MATERIALIZED ( "
            + "    SELECT "
            + "        f.festival_id, "
            + "        f.image_url, "
            + "        f.title, "
            + "        f.region, "
            + "        f.gungu, "
            + "        f.event_start_date, "
            + "        f.event_end_date, "
            + "        f.category, "
            + "        COALESCE(similarity(f.title, COALESCE(CAST(:keyword AS text), '')), 0) AS similarity_score "
            + "    FROM festivals f "
            + "    WHERE f.image_url IS NOT NULL AND f.image_url <> '' "
            + "    AND ( (:includeOngoing = true AND f.event_start_date <= :now AND f.event_end_date >= :now) "
            + "          OR (:includeUpcoming = true AND f.event_start_date > :now) ) "
            + "    AND ( CAST(:keyword AS text) IS NULL OR f.title ILIKE '%' || :keyword || '%' ) "
            + ") "
            + "SELECT "
            + "    festival_id AS festivalId, "
            + "    image_url AS imageUrl, "
            + "    title AS title, "
            + "    region AS region, "
            + "    gungu AS gungu, "
            + "    event_start_date AS eventStartDate, "
            + "    event_end_date AS eventEndDate, "
            + "    category AS category, "
            + "    similarity_score AS similarityScore "
            + "FROM candidates "
            + "WHERE similarity_score < :cursorSimilarity "
            + "   OR (similarity_score = :cursorSimilarity AND event_end_date > :cursorEventEndDate) "
            + "   OR (similarity_score = :cursorSimilarity AND event_end_date = :cursorEventEndDate AND festival_id > :cursorId) "
            + "ORDER BY similarity_score DESC, event_end_date ASC, festival_id ASC "
            + "LIMIT :limit",
            nativeQuery = true)
    List<FestivalSearchRow> searchFestivals(
            @Param("keyword") String keyword,
            @Param("includeOngoing") boolean includeOngoing,
            @Param("includeUpcoming") boolean includeUpcoming,
            @Param("now") Instant now,
            @Param("cursorSimilarity") double cursorSimilarity,
            @Param("cursorEventEndDate") Instant cursorEventEndDate,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );
}

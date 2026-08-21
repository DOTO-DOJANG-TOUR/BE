package com.doto.domain.festival.repository;

import com.doto.domain.festival.entity.Festival;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    Optional<Festival> findByContentId(Long contentId);

    // 커서 파라미터가 모두 null이면 첫 페이지, 아니면 정렬 기준(eventEndDate, eventStartDate, title) 다음부터 조회
    // title 비교는 FestivalCursor와 동일하게 공백/특수문자를 제거한 뒤 비교 (커서 값이 정규화된 접두 6글자이므로)
    @Query("SELECT f FROM Festival f "
            + "WHERE f.eventStartDate <= :now "
            + "AND f.eventEndDate >= :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (:cursorEventEndDate IS NULL "
            + "     OR f.eventEndDate > :cursorEventEndDate "
            + "     OR (f.eventEndDate = :cursorEventEndDate AND f.eventStartDate < :cursorEventStartDate) "
            + "     OR (f.eventEndDate = :cursorEventEndDate AND f.eventStartDate = :cursorEventStartDate "
            + "         AND function('regexp_replace', f.title, '[^가-힣a-zA-Z0-9]', '', 'g') > :cursorTitle)) "
            + "ORDER BY f.eventEndDate ASC, f.eventStartDate DESC, f.title ASC")
    List<Festival> findTodayFestivals(
            @Param("now") Instant now,
            @Param("cursorEventEndDate") Instant cursorEventEndDate,
            @Param("cursorEventStartDate") Instant cursorEventStartDate,
            @Param("cursorTitle") String cursorTitle,
            Pageable pageable
    );
}

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

    // 커서가 없는 첫 페이지는 서비스에서 cursorEventEndDate/cursorEventStartDate에 Instant.EPOCH, cursorTitle에 ""를 넘겨서 전체가 걸리도록 함
    // title 비교는 FestivalCursor와 동일하게 공백/특수문자를 제거한 뒤 비교 (커서 값이 정규화된 접두 6글자이므로)
    @Query("SELECT f FROM Festival f "
            + "WHERE f.eventStartDate <= :now "
            + "AND f.eventEndDate >= :now "
            + "AND f.imageUrl IS NOT NULL "
            + "AND f.imageUrl <> '' "
            + "AND (f.eventEndDate > :cursorEventEndDate "
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

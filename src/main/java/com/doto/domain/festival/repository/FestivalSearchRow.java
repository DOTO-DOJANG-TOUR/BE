package com.doto.domain.festival.repository;

import java.time.Instant;

// FestivalRepository.searchFestivals 네이티브 쿼리 결과 projection.
// region/gungu/category는 화면 표시에 필요한 만큼만 select하고, similarityScore는 정렬·커서용으로 쿼리에서 계산해 내려준다.
public interface FestivalSearchRow {

    Long getFestivalId();

    String getImageUrl();

    String getTitle();

    String getRegion();

    String getGungu();

    Instant getEventStartDate();

    Instant getEventEndDate();

    String getCategory();

    Double getSimilarityScore();
}

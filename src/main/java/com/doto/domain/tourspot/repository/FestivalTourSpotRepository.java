package com.doto.domain.tourspot.repository;

import com.doto.domain.tourspot.entity.FestivalTourSpot;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalTourSpotRepository extends JpaRepository<FestivalTourSpot, Long> {

    @Query("SELECT festivalTourSpot FROM FestivalTourSpot festivalTourSpot "
            + "JOIN FETCH festivalTourSpot.tourSpot "
            + "WHERE festivalTourSpot.festival.id = :festivalId")
    List<FestivalTourSpot> findAllWithTourSpotByFestivalId(@Param("festivalId") Long festivalId);

    @Query("SELECT festivalTourSpot FROM FestivalTourSpot festivalTourSpot "
            + "JOIN FETCH festivalTourSpot.tourSpot "
            + "WHERE festivalTourSpot.festival.id = :festivalId "
            + "AND (:category IS NULL OR festivalTourSpot.tourSpot.category = :category) "
            + "ORDER BY festivalTourSpot.distanceMeters ASC")
    List<FestivalTourSpot> findAllWithTourSpotByFestivalIdAndCategory(
            @Param("festivalId") Long festivalId,
            @Param("category") String category
    );

    @Query("SELECT festivalTourSpot FROM FestivalTourSpot festivalTourSpot "
            + "JOIN FETCH festivalTourSpot.tourSpot "
            + "WHERE festivalTourSpot.festival.id = :festivalId "
            + "AND LOWER(festivalTourSpot.tourSpot.title) "
            + "LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "ORDER BY festivalTourSpot.distanceMeters ASC")
    List<FestivalTourSpot> searchAllWithTourSpotByFestivalIdAndKeyword(
            @Param("festivalId") Long festivalId,
            @Param("keyword") String keyword
    );

    boolean existsByFestival_IdAndTourSpot_Id(Long festivalId, Long tourSpotId);

    // 축제 장소로부터 거리 계산
    @Query(value = """
            SELECT ST_Distance(f.location, ts.location)
            FROM festivals f
            JOIN tour_spots ts ON ts.tour_spot_id = :tourSpotId
            WHERE f.festival_id = :festivalId
            """, nativeQuery = true)
    BigDecimal calculateDistanceMeters(@Param("festivalId") Long festivalId, @Param("tourSpotId") Long tourSpotId);
}

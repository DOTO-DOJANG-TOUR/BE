package com.doto.domain.tourspot.repository;

import com.doto.domain.tourspot.entity.TourSpot;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourSpotRepository extends JpaRepository<TourSpot, Long> {

    Optional<TourSpot> findByContentId(Long contentId);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM tour_spots tour_spot
                WHERE tour_spot.tour_spot_id = :tourSpotId
                  AND ST_DWithin(
                      tour_spot.location,
                      ST_SetSRID(
                          ST_MakePoint(
                              CAST(:mapX AS double precision),
                              CAST(:mapY AS double precision)
                          ),
                          4326
                      )::geography,
                      300
                  )
            )
            """, nativeQuery = true)
    boolean existsWithin300Meters(
            @Param("tourSpotId") Long tourSpotId,
            @Param("mapX") BigDecimal mapX,
            @Param("mapY") BigDecimal mapY
    );
}

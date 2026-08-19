package com.doto.domain.tourspot.repository;

import com.doto.domain.tourspot.entity.FestivalTourSpot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalTourSpotRepository extends JpaRepository<FestivalTourSpot, Long> {

    @Query("SELECT festivalTourSpot FROM FestivalTourSpot festivalTourSpot "
            + "JOIN FETCH festivalTourSpot.tourSpot "
            + "WHERE festivalTourSpot.festival.id = :festivalId")
    List<FestivalTourSpot> findAllWithTourSpotByFestivalId(@Param("festivalId") Long festivalId);

    boolean existsByFestival_IdAndTourSpot_Id(Long festivalId, Long tourSpotId);
}

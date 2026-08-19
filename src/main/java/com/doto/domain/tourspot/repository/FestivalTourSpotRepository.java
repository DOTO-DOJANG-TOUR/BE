package com.doto.domain.tourspot.repository;

import com.doto.domain.tourspot.entity.FestivalTourSpot;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalTourSpotRepository extends JpaRepository<FestivalTourSpot, Long> {

    @EntityGraph(attributePaths = "tourSpot")
    List<FestivalTourSpot> findAllByFestival_Id(Long festivalId);

    boolean existsByFestival_IdAndTourSpot_Id(Long festivalId, Long tourSpotId);
}

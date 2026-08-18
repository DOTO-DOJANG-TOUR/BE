package com.doto.domain.tourspot.repository;

import com.doto.domain.tourspot.entity.TourSpot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourSpotRepository extends JpaRepository<TourSpot, Long> {

    Optional<TourSpot> findByContentId(Long contentId);
}

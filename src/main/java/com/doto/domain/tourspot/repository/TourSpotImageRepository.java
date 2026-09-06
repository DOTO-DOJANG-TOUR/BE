package com.doto.domain.tourspot.repository;

import com.doto.domain.tourspot.entity.TourSpotImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourSpotImageRepository extends JpaRepository<TourSpotImage, Long> {

    List<TourSpotImage> findAllByTourSpot_IdOrderBySerialNumberAsc(Long tourSpotId);

    void deleteAllByTourSpot_IdIn(List<Long> tourSpotIds);
}

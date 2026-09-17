package com.doto.domain.tourspot.repository;

import com.doto.domain.tourspot.entity.TourSpotImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourSpotImageRepository extends JpaRepository<TourSpotImage, Long> {

    @Query("SELECT image.imageUrl FROM TourSpotImage image "
            + "WHERE image.tourSpot.id = :tourSpotId ORDER BY image.serialNumber ASC")
    List<String> findImageUrlsByTourSpotId(@Param("tourSpotId") Long tourSpotId);

    void deleteAllByTourSpot_IdIn(List<Long> tourSpotIds);
}

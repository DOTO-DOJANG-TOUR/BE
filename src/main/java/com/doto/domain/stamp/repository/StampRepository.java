package com.doto.domain.stamp.repository;

import com.doto.domain.stamp.entity.Stamp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StampRepository extends JpaRepository<Stamp, Long> {

    Optional<Stamp> findByStampTour_IdAndTourSpot_Id(Long stampTourId, Long tourSpotId);
}

package com.doto.domain.tourspot.entity;

import com.doto.domain.festival.entity.Festival;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "festival_tour_spots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalTourSpot {

    @Id
    @Tsid
    @Column(name = "festival_tour_spot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_spot_id", nullable = false)
    private TourSpot tourSpot;

    @Column(name = "distance_meters", precision = 10, scale = 1)
    private BigDecimal distanceMeters;

    private FestivalTourSpot(Festival festival, TourSpot tourSpot, BigDecimal distanceMeters) {
        this.festival = festival;
        this.tourSpot = tourSpot;
        this.distanceMeters = distanceMeters;
    }

    public static FestivalTourSpot create(Festival festival, TourSpot tourSpot, BigDecimal distanceMeters) {
        return new FestivalTourSpot(festival, tourSpot, distanceMeters);
    }
}

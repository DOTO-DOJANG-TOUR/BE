package com.doto.domain.stamp.entity;

import com.doto.domain.festival.entity.TourSpot;
import com.doto.global.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "stamps",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stamps_stamp_tour_tour_spot",
                columnNames = {"stamp_tour_id", "tour_spot_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stamp extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "stamp_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stamp_tour_id", nullable = false)
    private StampTour stampTour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_spot_id", nullable = false)
    private TourSpot tourSpot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StampStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Stamp(StampTour stampTour, TourSpot tourSpot) {
        this.stampTour = stampTour;
        this.tourSpot = tourSpot;
        this.status = StampStatus.VISITING;
        this.startedAt = Instant.now();
    }

    public static Stamp create(StampTour stampTour, TourSpot tourSpot) {
        return Stamp.builder()
                .stampTour(stampTour)
                .tourSpot(tourSpot)
                .build();
    }

    public void complete() {
        this.status = StampStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        this.status = StampStatus.CANCELED;
    }
}

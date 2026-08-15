package com.doto.domain.festival.entity;

import com.doto.global.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Getter
@Entity
@Table(name = "tour_spots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourSpot extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "tour_spot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "category", length = 50)
    private String category;

    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(name = "location", columnDefinition = "geography(Point,4326)")
    private Point location;

    @Builder(access = AccessLevel.PRIVATE)
    private TourSpot(Festival festival, String title, String category, Point location) {
        this.festival = festival;
        this.title = title;
        this.category = category;
        this.location = location;
    }

    public static TourSpot create(Festival festival, String title, String category, Point location) {
        return TourSpot.builder()
                .festival(festival)
                .title(title)
                .category(category)
                .location(location)
                .build();
    }
}

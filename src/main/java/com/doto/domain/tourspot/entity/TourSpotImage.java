package com.doto.domain.tourspot.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tour_spot_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourSpotImage {

    @Id
    @Tsid
    @Column(name = "tour_spot_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_spot_id", nullable = false)
    private TourSpot tourSpot;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "image_name", length = 255)
    private String imageName;

    @Column(name = "serial_number")
    private Integer serialNumber;

    private TourSpotImage(
            TourSpot tourSpot,
            String imageUrl,
            String thumbnailUrl,
            String imageName,
            Integer serialNumber
    ) {
        this.tourSpot = tourSpot;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.imageName = imageName;
        this.serialNumber = serialNumber;
    }

    public static TourSpotImage create(
            TourSpot tourSpot,
            String imageUrl,
            String thumbnailUrl,
            String imageName,
            Integer serialNumber
    ) {
        return new TourSpotImage(tourSpot, imageUrl, thumbnailUrl, imageName, serialNumber);
    }
}

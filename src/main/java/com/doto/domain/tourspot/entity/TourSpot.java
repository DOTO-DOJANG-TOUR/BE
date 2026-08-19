package com.doto.domain.tourspot.entity;

import com.doto.global.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Column(name = "content_id", unique = true)
    private Long contentId;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "l_dong_regn_cd", length = 10)
    private String legalDongRegionCode;

    @Column(name = "l_dong_signgu_cd", length = 10)
    private String legalDongSigunguCode;

    @Column(name = "phone", length = 100)
    private String phone;

    @Column(name = "api_modified_at", length = 14)
    private String apiModifiedAt;

    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(name = "location", columnDefinition = "geography(Point,4326)")
    private Point location;

    @Builder(access = AccessLevel.PRIVATE)
    private TourSpot(
            Long contentId,
            String title,
            String category,
            String imageUrl,
            String address,
            String legalDongRegionCode,
            String legalDongSigunguCode,
            String phone,
            String apiModifiedAt,
            Point location
    ) {
        this.contentId = contentId;
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.address = address;
        this.legalDongRegionCode = legalDongRegionCode;
        this.legalDongSigunguCode = legalDongSigunguCode;
        this.phone = phone;
        this.apiModifiedAt = apiModifiedAt;
        this.location = location;
    }

    public static TourSpot create(
            Long contentId,
            String title,
            String category,
            String imageUrl,
            String address,
            String legalDongRegionCode,
            String legalDongSigunguCode,
            String phone,
            String apiModifiedAt,
            Point location
    ) {
        return TourSpot.builder()
                .contentId(contentId)
                .title(title)
                .category(category)
                .imageUrl(imageUrl)
                .address(address)
                .legalDongRegionCode(legalDongRegionCode)
                .legalDongSigunguCode(legalDongSigunguCode)
                .phone(phone)
                .apiModifiedAt(apiModifiedAt)
                .location(location)
                .build();
    }

    public void update(
            String title,
            String category,
            String imageUrl,
            String address,
            String legalDongRegionCode,
            String legalDongSigunguCode,
            String phone,
            String apiModifiedAt,
            Point location
    ) {
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.address = address;
        this.legalDongRegionCode = legalDongRegionCode;
        this.legalDongSigunguCode = legalDongSigunguCode;
        this.phone = phone;
        this.apiModifiedAt = apiModifiedAt;
        this.location = location;
    }
}

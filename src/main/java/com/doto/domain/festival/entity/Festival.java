package com.doto.domain.festival.entity;

import com.doto.global.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Getter
@Entity
@Table(name = "festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "festival_id")
    private Long id;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "homepage_url", columnDefinition = "TEXT")
    private String homepageUrl;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "play_time", length = 20)
    private String playTime;

    @Column(name = "rest_date", length = 50)
    private String restDate;

    @Column(name = "use_fee", length = 50)
    private String useFee;

    @Column(name = "parking", length = 50)
    private String parking;

    @Column(name = "parking_fee", length = 50)
    private String parkingFee;

    @Column(name = "area_code", nullable = false, length = 10)
    private String areaCode;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "sigungu_code", nullable = false, length = 10)
    private String sigunguCode;

    @Column(name = "event_start_date", nullable = false)
    private Instant eventStartDate;

    @Column(name = "event_end_date", nullable = false)
    private Instant eventEndDate;

    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(name = "location", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point location;

    @Builder(access = AccessLevel.PRIVATE)
    private Festival(
            String title,
            String summary,
            String homepageUrl,
            String category,
            String phone,
            String address,
            String playTime,
            String restDate,
            String useFee,
            String parking,
            String parkingFee,
            String areaCode,
            String regionCode,
            String sigunguCode,
            Instant eventStartDate,
            Instant eventEndDate,
            Point location
    ) {
        this.title = title;
        this.summary = summary;
        this.homepageUrl = homepageUrl;
        this.category = category;
        this.phone = phone;
        this.address = address;
        this.playTime = playTime;
        this.restDate = restDate;
        this.useFee = useFee;
        this.parking = parking;
        this.parkingFee = parkingFee;
        this.areaCode = areaCode;
        this.regionCode = regionCode;
        this.sigunguCode = sigunguCode;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.location = location;
    }

    public static Festival create(
            String title,
            String summary,
            String homepageUrl,
            String category,
            String phone,
            String address,
            String playTime,
            String restDate,
            String useFee,
            String parking,
            String parkingFee,
            String areaCode,
            String regionCode,
            String sigunguCode,
            Instant eventStartDate,
            Instant eventEndDate,
            Point location
    ) {
        return Festival.builder()
                .title(title)
                .summary(summary)
                .homepageUrl(homepageUrl)
                .category(category)
                .phone(phone)
                .address(address)
                .playTime(playTime)
                .restDate(restDate)
                .useFee(useFee)
                .parking(parking)
                .parkingFee(parkingFee)
                .areaCode(areaCode)
                .regionCode(regionCode)
                .sigunguCode(sigunguCode)
                .eventStartDate(eventStartDate)
                .eventEndDate(eventEndDate)
                .location(location)
                .build();
    }
}

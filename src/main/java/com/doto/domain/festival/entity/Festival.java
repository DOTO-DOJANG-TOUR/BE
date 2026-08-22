package com.doto.domain.festival.entity;

import com.doto.domain.festival.entity.enums.Region;
import com.doto.global.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Column(name = "content_id", unique = true)
    private Long contentId;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    //firstimage → 없으면 firstimage2 저장하도록 했는데 후에 이미지 별로 얼마나 적절한지 판단하는 기능 개선이 가능할 듯
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "homepage_url", columnDefinition = "TEXT")
    private String homepageUrl;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "phone", length = 100)
    private String phone;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "operation_hours", columnDefinition = "TEXT")
    private String operationHours;

    @Column(name = "rest_date", length = 50)
    private String restDate;

    @Column(name = "use_fee", length = 50)
    private String useFee;

    @Column(name = "parking", length = 50)
    private String parking;

    @Column(name = "parking_fee", length = 50)
    private String parkingFee;

    @Column(name = "program", columnDefinition = "TEXT")
    private String program;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", length = 20)
    private Region legalRegion;

    @Column(name = "gungu", length = 20)
    private String legalGungu;

    @Column(name = "event_start_date", nullable = false)
    private Instant eventStartDate;

    @Column(name = "event_end_date", nullable = false)
    private Instant eventEndDate;

    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(name = "location", nullable = false, columnDefinition = "geography(Point,4326)")
    private Point location;

    @Builder(access = AccessLevel.PRIVATE)
    private Festival(
            Long contentId,
            String title,
            String summary,
            String imageUrl,
            String homepageUrl,
            String category,
            String phone,
            String address,
            String operationHours,
            String restDate,
            String useFee,
            String parking,
            String parkingFee,
            String program,
            Region legalRegion,
            String legalGungu,
            Instant eventStartDate,
            Instant eventEndDate,
            Point location
    ) {
        this.contentId = contentId;
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.homepageUrl = homepageUrl;
        this.category = category;
        this.phone = phone;
        this.address = address;
        this.operationHours = operationHours;
        this.restDate = restDate;
        this.useFee = useFee;
        this.parking = parking;
        this.parkingFee = parkingFee;
        this.program = program;
        this.legalRegion = legalRegion;
        this.legalGungu = legalGungu;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.location = location;
    }

    public static Festival create(
            Long contentId,
            String title,
            String summary,
            String imageUrl,
            String homepageUrl,
            String category,
            String phone,
            String address,
            String operationHours,
            String restDate,
            String useFee,
            String parking,
            String parkingFee,
            String program,
            Region legalRegion,
            String legalGungu,
            Instant eventStartDate,
            Instant eventEndDate,
            Point location
    ) {
        return Festival.builder()
                .contentId(contentId)
                .title(title)
                .summary(summary)
                .imageUrl(imageUrl)
                .homepageUrl(homepageUrl)
                .category(category)
                .phone(phone)
                .address(address)
                .operationHours(operationHours)
                .restDate(restDate)
                .useFee(useFee)
                .parking(parking)
                .parkingFee(parkingFee)
                .program(program)
                .legalRegion(legalRegion)
                .legalGungu(legalGungu)
                .eventStartDate(eventStartDate)
                .eventEndDate(eventEndDate)
                .location(location)
                .build();
    }

    public void update(
            String title,
            String summary,
            String imageUrl,
            String homepageUrl,
            String category,
            String phone,
            String address,
            String operationHours,
            String restDate,
            String useFee,
            String parking,
            String parkingFee,
            String program,
            Region legalRegion,
            String legalGungu,
            Instant eventStartDate,
            Instant eventEndDate,
            Point location
    ) {
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.homepageUrl = homepageUrl;
        this.category = category;
        this.phone = phone;
        this.address = address;
        this.operationHours = operationHours;
        this.restDate = restDate;
        this.useFee = useFee;
        this.parking = parking;
        this.parkingFee = parkingFee;
        this.program = program;
        this.legalRegion = legalRegion;
        this.legalGungu = legalGungu;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.location = location;
    }
}

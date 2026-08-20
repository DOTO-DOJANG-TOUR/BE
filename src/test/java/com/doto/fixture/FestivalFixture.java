package com.doto.fixture;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.enums.Region;
import com.doto.domain.festival.entity.enums.RegionGroup;
import java.time.Instant;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public final class FestivalFixture {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private FestivalFixture() {
    }

    public static Festival create() {
        return create(2515245L);
    }

    public static Festival create(Long contentId) {
        return Festival.create(
                contentId,
                "도토 축제",
                "도토 축제 소개",
                "https://doto.example.com/festivals.jpg",
                "https://doto.example.com/festivals",
                "FESTIVAL",
                "02-1234-5678",
                "서울특별시 강남구 도토로 1",
                "18:00~19:30",
                "10:00~22:00",
                "월요일",
                "무료",
                "가능",
                "무료",
                "주요 프로그램",
                Region.서울특별시,
                RegionGroup.서울,
                Instant.parse("2026-08-15T00:00:00Z"),
                Instant.parse("2026-08-20T00:00:00Z"),
                GEOMETRY_FACTORY.createPoint(new Coordinate(127.0280, 37.4980))
        );
    }
}

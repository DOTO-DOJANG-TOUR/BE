package com.doto.domain.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

class FestivalTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Nested
    class 생성 {

        @Test
        void 축제_정보와_WGS84_위치로_생성된다() {
            Point location = GEOMETRY_FACTORY.createPoint(new Coordinate(127.0276, 37.4979));

            Festival festival = Festival.create(
                    "도토 축제", "축제 소개", "https://doto.example.com", "FESTIVAL", "02-1234-5678", "서울시 강남구",
                    "10:00~22:00", "월요일", "무료", "가능", "무료", "1", "11", "11680",
                    Instant.parse("2026-08-15T00:00:00Z"), Instant.parse("2026-08-20T00:00:00Z"), location
            );

            assertThat(festival.getTitle()).isEqualTo("도토 축제");
            assertThat(festival.getAreaCode()).isEqualTo("1");
            assertThat(festival.getEventStartDate()).isEqualTo(Instant.parse("2026-08-15T00:00:00Z"));
            assertThat(festival.getLocation()).isEqualTo(location);
            assertThat(festival.getLocation().getSRID()).isEqualTo(4326);
        }
    }
}

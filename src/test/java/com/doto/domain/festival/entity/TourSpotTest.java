package com.doto.domain.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

class TourSpotTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Nested
    class 생성 {

        @Test
        void 축제와_관광지_정보로_생성된다() {
            Festival festival = createFestival();
            Point location = GEOMETRY_FACTORY.createPoint(new Coordinate(127.0280, 37.4980));

            TourSpot tourSpot = TourSpot.create(festival, "도토 광장", "관광지", location);

            assertThat(tourSpot.getFestival()).isEqualTo(festival);
            assertThat(tourSpot.getTitle()).isEqualTo("도토 광장");
            assertThat(tourSpot.getLocation()).isEqualTo(location);
        }
    }

    private Festival createFestival() {
        return Festival.create(
                "도토 축제", null, null, null, null, null, null, null, null, null, null, "1", null, "11680",
                Instant.parse("2026-08-15T00:00:00Z"), Instant.parse("2026-08-20T00:00:00Z"),
                GEOMETRY_FACTORY.createPoint(new Coordinate(127.0276, 37.4979))
        );
    }
}

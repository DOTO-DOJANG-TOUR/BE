package com.doto.domain.tourspot.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.festival.entity.Festival;
import com.doto.domain.festival.entity.enums.Region;
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

            TourSpot tourSpot = TourSpot.create(
                    125405L, "도토 광장", "관광지", "https://doto.example.com/image.jpg", "서울시 강남구",
                    "11", "11000", "02-1234-5678", "20260819090000", location
            );

            assertThat(tourSpot.getContentId()).isEqualTo(125405L);
            assertThat(tourSpot.getContentId()).isEqualTo(125405L);
            assertThat(tourSpot.getTitle()).isEqualTo("도토 광장");
            assertThat(tourSpot.getAddress()).isEqualTo("서울시 강남구");
            assertThat(tourSpot.getApiModifiedAt()).isEqualTo("20260819090000");
            assertThat(tourSpot.getLocation()).isEqualTo(location);
        }
    }

    private Festival createFestival() {
        return Festival.create(
                2515245L, "도토 축제", null, null, null, null, null, null, null, null, null, null, null, null, null, Region.서울특별시, "680",
                Instant.parse("2026-08-15T00:00:00Z"), Instant.parse("2026-08-20T00:00:00Z"),
                GEOMETRY_FACTORY.createPoint(new Coordinate(127.0276, 37.4979))
        );
    }
}

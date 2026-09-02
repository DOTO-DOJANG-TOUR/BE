package com.doto.fixture;

import com.doto.domain.stamp.dto.TourSpotItemDetailResponseDTO;
import com.doto.domain.tourspot.entity.enums.TourSpotCategory;
import com.doto.domain.tourspot.entity.TourSpot;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public final class TourSpotFixture {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private TourSpotFixture() {
    }

    public static TourSpot create() {
        return create(125405L);
    }

    public static TourSpot create(Long contentId) {
        return TourSpot.create(
                contentId,
                "도토 광장",
                TourSpotCategory.문화.name(),
                "https://doto.example.com/tour-spots.jpg",
                "서울특별시 강남구 도토길 1",
                "11",
                "11000",
                "02-9876-5432",
                "20260819090000",
                GEOMETRY_FACTORY.createPoint(new Coordinate(127.0280, 37.4980))
        );
    }

    public static TourSpotItemDetailResponseDTO createResponseDTO() {
        return createResponseDTO(125405L);
    }

    public static TourSpotItemDetailResponseDTO createResponseDTO(Long contentId) {
        return new TourSpotItemDetailResponseDTO(
                null,
                contentId,
                "도토 광장",
                "서울특별시 강남구 도토길 1",
                "https://doto.example.com/tour-spots.jpg",
                "127.0280",
                "37.4980",
                TourSpotCategory.문화,
                "11000",
                "02-9876-5432",
                "20260819090000"
        );
    }
}

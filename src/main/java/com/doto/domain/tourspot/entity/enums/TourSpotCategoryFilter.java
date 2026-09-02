package com.doto.domain.tourspot.entity.enums;

public enum TourSpotCategoryFilter {
    ALL,
    문화,
    역사,
    자연,
    체험;

    public String categoryNameOrNull() {
        return this == ALL ? null : name();
    }
}

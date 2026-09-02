package com.doto.domain.tourspot.entity.enums;

import com.doto.domain.tourex.enums.TourApiCategory;

public enum TourSpotCategory {
    문화,
    역사,
    자연,
    체험;

    public static TourSpotCategory from(TourApiCategory category) {
        if (category == null) {
            return null;
        }

        return switch (category) {
            case 문화관광 -> 문화;
            case 역사관광 -> 역사;
            case 자연관광 -> 자연;
            case 체험관광 -> 체험;
            default -> null;
        };
    }
}

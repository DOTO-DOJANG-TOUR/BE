package com.doto.global.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DistanceUtils {

    private static final BigDecimal ONE_KILOMETER_IN_METERS = BigDecimal.valueOf(1_000);

    private DistanceUtils() {
    }

    public static String format(BigDecimal distanceInMeters) {
        if (distanceInMeters == null) {
            return null;
        }

        if (distanceInMeters.compareTo(ONE_KILOMETER_IN_METERS) < 0) {
            return distanceInMeters.setScale(0, RoundingMode.HALF_UP).toPlainString() + "m";
        }

        return distanceInMeters.divide(ONE_KILOMETER_IN_METERS, 1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "km";
    }
}

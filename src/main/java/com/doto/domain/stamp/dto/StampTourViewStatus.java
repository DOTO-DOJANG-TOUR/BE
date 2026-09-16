package com.doto.domain.stamp.dto;

import com.doto.domain.stamp.entity.enums.StampTourStatus;

public enum StampTourViewStatus {
    FESTIVAL_ENDED,
    PARTICIPATING_IN_ANOTHER_TOUR,
    NOT_STARTED,
    PROGRESS,
    COMPLETED,
    REWARDED;

    public static StampTourViewStatus from(StampTourStatus status) {
        if (status == null) {
            return NOT_STARTED;
        }
        if (status == StampTourStatus.ENDED) {
            return FESTIVAL_ENDED;
        }
        return StampTourViewStatus.valueOf(status.name());
    }
}

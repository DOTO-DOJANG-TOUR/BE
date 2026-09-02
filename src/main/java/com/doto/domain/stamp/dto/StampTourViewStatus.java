package com.doto.domain.stamp.dto;

import com.doto.domain.stamp.entity.enums.StampTourStatus;

public enum StampTourViewStatus {
    NOT_STARTED,
    PROGRESS,
    COMPLETED,
    REWARDED;

    public static StampTourViewStatus from(StampTourStatus status) {
        return status == null ? NOT_STARTED : StampTourViewStatus.valueOf(status.name());
    }
}

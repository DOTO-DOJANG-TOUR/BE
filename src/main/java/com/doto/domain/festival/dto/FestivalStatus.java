package com.doto.domain.festival.dto;

// 지역별 축제 응답에서만 쓰는 파생 상태값, eventStartDate<=now면 개최중
public enum FestivalStatus {
    ONGOING,
    UPCOMING
}

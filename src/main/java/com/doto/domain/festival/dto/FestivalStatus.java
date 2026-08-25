package com.doto.domain.festival.dto;

// eventStartDate<=now<=eventEndDate면 개최중, eventStartDate>now면 개최전, eventEndDate<now면 종료
public enum FestivalStatus {
    ONGOING,
    UPCOMING,
    ENDED
}

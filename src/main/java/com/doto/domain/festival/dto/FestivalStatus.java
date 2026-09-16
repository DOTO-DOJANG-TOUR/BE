package com.doto.domain.festival.dto;

// eventStartDate<=now<=eventEndDate면 개최중, eventStartDate>now면 개최전, eventEndDate<now면 종료
public enum FestivalStatus {

    ONGOING("개최중"),
    UPCOMING("개최전"),
    ENDED("종료");

    private final String displayName;

    FestivalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

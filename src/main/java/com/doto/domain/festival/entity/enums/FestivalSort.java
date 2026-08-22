package com.doto.domain.festival.entity.enums;

// 지역별 축제 정렬 기준, API 파라미터로 쓰여서 상수명은 영어 + 표시용 한글명 별도 보관
public enum FestivalSort {

    END_DATE("종료임박순"),
    START_DATE("개최임박순");

    private final String displayName;

    FestivalSort(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

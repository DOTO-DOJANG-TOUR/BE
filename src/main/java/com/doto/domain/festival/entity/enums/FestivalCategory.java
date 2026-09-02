package com.doto.domain.festival.entity.enums;

import java.util.Arrays;

// 축제 소분류(lclsSystm3) 매핑, TourAPI 축제(EV01) 소분류 코드 전용 (festival 도메인에서만 사용)
public enum FestivalCategory {

    문화관광("EV010100"),
    문화예술("EV010200"),
    지역특산물("EV010300"),
    전통역사("EV010400"),
    생태자연("EV010500"),
    기타("EV010600");

    private final String code;

    FestivalCategory(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static FestivalCategory fromLclsSystem3Code(String lclsSystem3) {
        return Arrays.stream(values())
                .filter(category -> category.code.equals(lclsSystem3))
                .findFirst()
                .orElse(null);
    }
}

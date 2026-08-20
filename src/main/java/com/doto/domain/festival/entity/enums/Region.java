package com.doto.domain.festival.entity.enums;

import java.util.Arrays;

public enum Region {

    서울특별시("11"),
    전남광주통합특별시("12"),
    부산광역시("26"),
    대구광역시("27"),
    인천광역시("28"),
    대전광역시("30"),
    울산광역시("31"),
    경기도("41"),
    충청북도("43"),
    충청남도("44"),
    경상북도("47"),
    경상남도("48"),
    제주특별자치도("50"),
    강원특별자치도("51"),
    전북특별자치도("52"),
    세종특별자치시("36110");

    private final String code;

    Region(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Region fromCode(String code) {
        return Arrays.stream(values())
                .filter(region -> region.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}

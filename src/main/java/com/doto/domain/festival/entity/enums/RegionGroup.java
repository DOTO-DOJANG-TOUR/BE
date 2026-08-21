package com.doto.domain.festival.entity.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

// 정해진 시도를 10개로 줄여서 매핑하기 위해 사용
public enum RegionGroup {

    서울(EnumSet.of(Region.서울특별시)),
    경기_인천(EnumSet.of(Region.경기도, Region.인천광역시)),
    강원(EnumSet.of(Region.강원특별자치도)),
    충북(EnumSet.of(Region.충청북도)),
    충남권(EnumSet.of(Region.충청남도, Region.대전광역시, Region.세종특별자치시)),
    전북(EnumSet.of(Region.전북특별자치도)),
    전남권(EnumSet.of(Region.전남광주통합특별시)),
    경북권(EnumSet.of(Region.경상북도, Region.대구광역시)),
    경남권(EnumSet.of(Region.경상남도, Region.부산광역시, Region.울산광역시)),
    제주(EnumSet.of(Region.제주특별자치도));

    private final Set<Region> regions;

    RegionGroup(Set<Region> regions) {
        this.regions = regions;
    }

    public Set<Region> getRegions() {
        return regions;
    }

    public static RegionGroup from(Region region) {
        if (region == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(group -> group.regions.contains(region))
                .findFirst()
                .orElse(null);
    }
}

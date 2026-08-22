package com.doto.domain.festival.entity.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

// 정해진 시도를 10개로 줄여서 매핑, API 파라미터로 쓰여서 상수명은 영어 + 표시용 한글명 별도 보관
public enum RegionGroup {

    SEOUL("서울", EnumSet.of(Region.서울특별시)),
    GYEONGGI_INCHEON("경기·인천", EnumSet.of(Region.경기도, Region.인천광역시)),
    GANGWON("강원", EnumSet.of(Region.강원특별자치도)),
    CHUNGBUK("충북", EnumSet.of(Region.충청북도)),
    CHUNGNAM("충남권", EnumSet.of(Region.충청남도, Region.대전광역시, Region.세종특별자치시)),
    JEONBUK("전북", EnumSet.of(Region.전북특별자치도)),
    JEONNAM("전남권", EnumSet.of(Region.전남광주통합특별시)),
    GYEONGBUK("경북권", EnumSet.of(Region.경상북도, Region.대구광역시)),
    GYEONGNAM("경남권", EnumSet.of(Region.경상남도, Region.부산광역시, Region.울산광역시)),
    JEJU("제주", EnumSet.of(Region.제주특별자치도));

    private final String displayName;
    private final Set<Region> regions;

    RegionGroup(String displayName, Set<Region> regions) {
        this.displayName = displayName;
        this.regions = regions;
    }

    public String getDisplayName() {
        return displayName;
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

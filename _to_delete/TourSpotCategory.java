package com.doto.domain.stamp.entity.enums;

public enum TourSpotCategory {
    문화관광,
    역사관광,
    자연관광,
    체험관광,
    레저스포츠,
    숙박,
    음식,
    쇼핑,
    축제공연행사,
    추천코스;

    // 대분류(lclsSystm1) 코드 매핑, 관광공사 lclsSystmCode2 10개 코드 전체를 한글 카테고리명으로 매핑, 매핑 안 되면 null
    public static TourSpotCategory fromLclsSystem1Code(String lclsSystem1) {
        return switch (lclsSystem1) {
            case "VE" -> 문화관광;
            case "HS" -> 역사관광;
            case "NA" -> 자연관광;
            case "EX" -> 체험관광;
            case "LS" -> 레저스포츠;
            case "AC" -> 숙박;
            case "FD" -> 음식;
            case "SH" -> 쇼핑;
            case "EV" -> 축제공연행사;
            case "C01" -> 추천코스;
            case null, default -> null;
        };
    }
}

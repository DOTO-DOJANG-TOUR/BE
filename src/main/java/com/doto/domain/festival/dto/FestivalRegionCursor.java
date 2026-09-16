package com.doto.domain.festival.dto;

import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

// 지역별 축제 목록 정렬 기준(tier, rankDate, id) opaque 인코딩. tier는 상태 우선순위(0이 1순위)
public record FestivalRegionCursor(int tier, Instant rankDate, Long id) {

    private static final String DELIMITER = "|";

    public String encode() {
        String raw = tier + DELIMITER + rankDate + DELIMITER + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FestivalRegionCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 3);
            return new FestivalRegionCursor(Integer.parseInt(parts[0]), Instant.parse(parts[1]), Long.parseLong(parts[2]));
        } catch (RuntimeException exception) {
            throw new FestivalException(FestivalErrorCode.INVALID_CURSOR);
        }
    }
}

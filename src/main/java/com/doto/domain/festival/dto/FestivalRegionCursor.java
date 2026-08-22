package com.doto.domain.festival.dto;

import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

// 지역별 축제 정렬 기준(eventEndDate, eventStartDate, title 정규화 앞 6글자) opaque 인코딩, 종료/개최임박순 공용
public record FestivalRegionCursor(Instant eventEndDate, Instant eventStartDate, String title) {

    private static final String DELIMITER = "|";
    private static final int TITLE_PREFIX_LENGTH = 6;
    private static final String NORMALIZE_PATTERN = "[^가-힣a-zA-Z0-9]";

    public String encode() {
        String normalized = title.replaceAll(NORMALIZE_PATTERN, "");
        String titlePrefix = normalized.substring(0, Math.min(TITLE_PREFIX_LENGTH, normalized.length()));
        String raw = eventEndDate + DELIMITER + eventStartDate + DELIMITER + titlePrefix;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FestivalRegionCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 3);
            return new FestivalRegionCursor(Instant.parse(parts[0]), Instant.parse(parts[1]), parts[2]);
        } catch (RuntimeException exception) {
            throw new FestivalException(FestivalErrorCode.INVALID_CURSOR);
        }
    }
}

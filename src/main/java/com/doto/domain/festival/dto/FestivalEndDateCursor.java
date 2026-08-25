package com.doto.domain.festival.dto;

import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

// 오늘의 축제 정렬 기준(eventEndDate, id) opaque 인코딩
public record FestivalEndDateCursor(Instant eventEndDate, Long id) {

    private static final String DELIMITER = "|";

    public String encode() {
        String raw = eventEndDate + DELIMITER + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FestivalEndDateCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 2);
            return new FestivalEndDateCursor(Instant.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (RuntimeException exception) {
            throw new FestivalException(FestivalErrorCode.INVALID_CURSOR);
        }
    }
}

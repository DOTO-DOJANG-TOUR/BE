package com.doto.domain.festival.dto;

import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

// 앞으로의 축제 정렬 기준(eventStartDate, 기간, id) opaque 인코딩
public record FestivalUpcomingCursor(Instant eventStartDate, Duration duration, Long id) {

    private static final String DELIMITER = "|";

    public String encode() {
        String raw = eventStartDate + DELIMITER + duration + DELIMITER + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FestivalUpcomingCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 3);
            return new FestivalUpcomingCursor(Instant.parse(parts[0]), Duration.parse(parts[1]), Long.parseLong(parts[2]));
        } catch (RuntimeException exception) {
            throw new FestivalException(FestivalErrorCode.INVALID_CURSOR);
        }
    }
}

package com.doto.domain.festival.dto;

import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.festival.exception.FestivalException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

// 축제 검색 정렬 기준(제목 유사도, eventEndDate, id) opaque 인코딩
public record FestivalSearchCursor(double similarity, Instant eventEndDate, Long id) {

    private static final String DELIMITER = "|";

    public String encode() {
        String raw = similarity + DELIMITER + eventEndDate + DELIMITER + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FestivalSearchCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 3);
            return new FestivalSearchCursor(
                    Double.parseDouble(parts[0]),
                    Instant.parse(parts[1]),
                    Long.parseLong(parts[2])
            );
        } catch (RuntimeException exception) {
            throw new FestivalException(FestivalErrorCode.INVALID_CURSOR);
        }
    }
}

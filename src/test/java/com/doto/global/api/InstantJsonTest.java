package com.doto.global.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.json.JsonMapper;

@JsonTest
class InstantJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("Instant를 UTC 기반 ISO 8601 문자열로 반환한다")
    void serializesInstantAsIso8601Utc() {
        SampleResponse response = new SampleResponse(Instant.parse("2026-08-02T09:30:00Z"));

        String json = jsonMapper.writeValueAsString(response);

        assertThat(json).isEqualTo("{\"createdAt\":\"2026-08-02T09:30:00Z\"}");
    }

    private record SampleResponse(Instant createdAt) {
    }
}

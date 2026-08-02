package com.doto.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpaqueTokenGeneratorTest {

    @Test
    void 생성할_때마다_다른_토큰을_반환한다() {
        String first = OpaqueTokenGenerator.generate();
        String second = OpaqueTokenGenerator.generate();

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotBlank();
    }

    @Test
    void 같은_원문의_해시는_항상_같다() {
        String raw = OpaqueTokenGenerator.generate();

        assertThat(OpaqueTokenGenerator.hash(raw)).isEqualTo(OpaqueTokenGenerator.hash(raw));
    }

    @Test
    void 다른_원문의_해시는_서로_다르다() {
        String first = OpaqueTokenGenerator.generate();
        String second = OpaqueTokenGenerator.generate();

        assertThat(OpaqueTokenGenerator.hash(first)).isNotEqualTo(OpaqueTokenGenerator.hash(second));
    }

    @Test
    void 해시값에는_원문이_그대로_노출되지_않는다() {
        String raw = OpaqueTokenGenerator.generate();

        assertThat(OpaqueTokenGenerator.hash(raw)).isNotEqualTo(raw);
    }
}

package com.doto.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HashTokenUtilTest {

    @Test
    void 생성할_때마다_다른_토큰을_반환한다() {
        String first = HashTokenUtil.generate();
        String second = HashTokenUtil.generate();

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotBlank();
    }

    @Test
    void 같은_원문의_해시는_항상_같다() {
        String raw = HashTokenUtil.generate();

        assertThat(HashTokenUtil.hash(raw)).isEqualTo(HashTokenUtil.hash(raw));
    }

    @Test
    void 다른_원문의_해시는_서로_다르다() {
        String first = HashTokenUtil.generate();
        String second = HashTokenUtil.generate();

        assertThat(HashTokenUtil.hash(first)).isNotEqualTo(HashTokenUtil.hash(second));
    }

    @Test
    void 해시값에는_원문이_그대로_노출되지_않는다() {
        String raw = HashTokenUtil.generate();

        assertThat(HashTokenUtil.hash(raw)).isNotEqualTo(raw);
    }
}

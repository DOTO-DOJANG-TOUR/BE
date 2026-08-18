package com.doto.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    private final Member member = Member.register("홍길동");

    @Nested
    class 사용_가능_여부 {

        @Test
        void 만료되지_않고_폐기되지_않았으면_사용_가능하다() {
            RefreshToken token = RefreshToken.issue(member, "hash", Instant.now().plusSeconds(60));

            assertThat(token.isUsable()).isTrue();
        }

        @Test
        void 만료_시각이_지나면_사용_불가능하다() {
            RefreshToken token = RefreshToken.issue(member, "hash", Instant.now().minusSeconds(1));

            assertThat(token.isUsable()).isFalse();
        }

        @Test
        void 폐기되면_사용_불가능하다() {
            RefreshToken token = RefreshToken.issue(member, "hash", Instant.now().plusSeconds(60));

            token.revoke();

            assertThat(token.isUsable()).isFalse();
            assertThat(token.getRevokedAt()).isNotNull();
        }
    }
}

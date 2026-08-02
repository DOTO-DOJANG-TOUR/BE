package com.doto.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GeneralAuthAccountTest {

    private final User user = User.register("홍길동");

    @Nested
    class 생성 {

        @Test
        void 사용자와_이메일_비밀번호해시로_생성된다() {
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "hashed");

            assertThat(account.getUser()).isEqualTo(user);
            assertThat(account.getEmail()).isEqualTo("user@example.com");
            assertThat(account.getPasswordHash()).isEqualTo("hashed");
        }
    }

    @Nested
    class 비밀번호_변경 {

        @Test
        void 비밀번호_해시를_교체할_수_있다() {
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "old-hash");

            account.changePassword("new-hash");

            assertThat(account.getPasswordHash()).isEqualTo("new-hash");
        }
    }

    @Nested
    class 이메일_변경 {

        @Test
        void 이메일을_교체할_수_있다() {
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "old@example.com", "hashed");

            account.changeEmail("new@example.com");

            assertThat(account.getEmail()).isEqualTo("new@example.com");
        }
    }
}

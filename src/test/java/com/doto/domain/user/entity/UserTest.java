package com.doto.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserTest {

    @Nested
    class 회원가입 {

        @Test
        void 닉네임으로_가입하면_상태는_ACTIVE이다() {
            User user = User.register("홍길동");

            assertThat(user.getNickname()).isEqualTo("홍길동");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    class 닉네임_수정 {

        @Test
        void 닉네임을_변경할_수_있다() {
            User user = User.register("홍길동");

            user.updateNickname("김철수");

            assertThat(user.getNickname()).isEqualTo("김철수");
        }
    }

    @Nested
    class 비활성화 {

        @Test
        void 비활성화하면_상태가_INACTIVE로_바뀐다() {
            User user = User.register("홍길동");

            user.deactivate();

            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        }
    }
}

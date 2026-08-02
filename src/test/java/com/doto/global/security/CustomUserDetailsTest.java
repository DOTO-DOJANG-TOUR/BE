package com.doto.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CustomUserDetailsTest {

    @Test
    void 활성_사용자는_enabled가_true다() {
        User user = User.register("홍길동");
        ReflectionTestUtils.setField(user, "id", 100L);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertThat(userDetails.getUserId()).isEqualTo(100L);
        assertThat(userDetails.getUsername()).isEqualTo("100");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getPassword()).isNull();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void 비활성화된_사용자는_enabled가_false다() {
        User user = User.register("홍길동");
        user.deactivate();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertThat(userDetails.isEnabled()).isFalse();
    }
}

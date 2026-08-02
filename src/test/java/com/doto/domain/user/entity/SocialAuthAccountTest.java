package com.doto.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SocialAuthAccountTest {

    @Test
    void 소셜_제공자_정보로_생성된다() {
        User user = User.register("홍길동");

        SocialAuthAccount account = SocialAuthAccount.create(
                user, SocialProvider.KAKAO, "https://kauth.kakao.com", "external-id-1", "user@example.com"
        );

        assertThat(account.getUser()).isEqualTo(user);
        assertThat(account.getProvider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(account.getIssuer()).isEqualTo("https://kauth.kakao.com");
        assertThat(account.getExternalId()).isEqualTo("external-id-1");
        assertThat(account.getEmail()).isEqualTo("user@example.com");
    }
}

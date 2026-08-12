package com.doto.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SocialAuthAccountTest {

    @Test
    void 소셜_제공자_정보로_생성된다() {
        Member member = Member.register("홍길동");

        SocialAuthAccount account = SocialAuthAccount.create(
                member, SocialProvider.KAKAO, "https://kauth.kakao.com", "external-id-1", "member@example.com"
        );

        assertThat(account.getMember()).isEqualTo(member);
        assertThat(account.getProvider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(account.getIssuer()).isEqualTo("https://kauth.kakao.com");
        assertThat(account.getExternalId()).isEqualTo("external-id-1");
        assertThat(account.getEmail()).isEqualTo("member@example.com");
    }

    @Test
    void 이메일을_갱신할_수_있다() {
        Member member = Member.register("홍길동");
        SocialAuthAccount account = SocialAuthAccount.create(
                member, SocialProvider.KAKAO, "https://kauth.kakao.com", "external-id-1", null
        );

        account.updateEmail("member@example.com");

        assertThat(account.getEmail()).isEqualTo("member@example.com");
    }
}

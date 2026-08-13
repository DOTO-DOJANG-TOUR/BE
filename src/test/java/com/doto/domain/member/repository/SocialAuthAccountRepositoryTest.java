package com.doto.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.member.entity.SocialAuthAccount;
import com.doto.domain.member.entity.SocialProvider;
import com.doto.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SocialAuthAccountRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SocialAuthAccountRepository socialAuthAccountRepository;

    @Test
    void provider와_externalId로_계정을_찾는다() {
        Member member = memberRepository.save(Member.register("홍길동"));
        socialAuthAccountRepository.save(
                SocialAuthAccount.create(member, SocialProvider.KAKAO, "issuer", "external-1", "member@example.com")
        );

        assertThat(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "external-1"))
                .isPresent();
        assertThat(socialAuthAccountRepository.existsByProviderAndExternalId(SocialProvider.KAKAO, "external-1"))
                .isTrue();
        assertThat(socialAuthAccountRepository.existsByProviderAndExternalId(SocialProvider.GOOGLE, "external-1"))
                .isFalse();
    }

    @Test
    void memberId로_계정을_찾는다() {
        Member member = memberRepository.save(Member.register("홍길동"));
        socialAuthAccountRepository.save(
                SocialAuthAccount.create(member, SocialProvider.KAKAO, "issuer", "external-1", "member@example.com")
        );

        assertThat(socialAuthAccountRepository.findByMember_Id(member.getId()))
                .isPresent()
                .get()
                .extracting(SocialAuthAccount::getEmail)
                .isEqualTo("member@example.com");
    }
}

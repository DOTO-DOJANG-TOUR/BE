package com.doto.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.member.entity.GeneralAuthAccount;
import com.doto.domain.member.entity.Member;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GeneralAuthAccountRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GeneralAuthAccountRepository generalAuthAccountRepository;

    private Member persistMember() {
        return memberRepository.save(Member.register("홍길동"));
    }

    @Nested
    class 이메일로_조회 {

        @Test
        void 저장된_이메일로_계정을_찾는다() {
            Member member = persistMember();
            generalAuthAccountRepository.save(GeneralAuthAccount.create(member, "member@example.com", "hash"));

            assertThat(generalAuthAccountRepository.findByEmail("member@example.com")).isPresent();
            assertThat(generalAuthAccountRepository.existsByEmail("member@example.com")).isTrue();
        }

        @Test
        void 존재하지_않는_이메일은_없다() {
            assertThat(generalAuthAccountRepository.existsByEmail("nobody@example.com")).isFalse();
            assertThat(generalAuthAccountRepository.findByEmail("nobody@example.com")).isEmpty();
        }
    }

    @Nested
    class 사용자ID로_조회 {

        @Test
        void 사용자_ID로_계정을_찾는다() {
            Member member = persistMember();
            generalAuthAccountRepository.save(GeneralAuthAccount.create(member, "member@example.com", "hash"));

            assertThat(generalAuthAccountRepository.findByMember_Id(member.getId())).isPresent();
        }
    }
}

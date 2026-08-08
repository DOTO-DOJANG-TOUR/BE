package com.doto.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 저장한_사용자를_ID로_다시_조회할_수_있다() {
        Member saved = memberRepository.save(Member.register("홍길동"));

        Member found = memberRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getNickname()).isEqualTo("홍길동");
        assertThat(found.getId()).isNotNull();
    }
}

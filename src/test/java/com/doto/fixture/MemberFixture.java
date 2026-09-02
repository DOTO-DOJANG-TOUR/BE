package com.doto.fixture;

import com.doto.domain.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member create() {
        return create(1L);
    }

    public static Member create(Long id) {
        Member member = Member.register("도토");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}

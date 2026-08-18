package com.doto.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CustomMemberDetailsTest {

    @Test
    void 활성_사용자는_enabled가_true다() {
        Member member = Member.register("홍길동");
        ReflectionTestUtils.setField(member, "id", 100L);

        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        assertThat(memberDetails.getMemberId()).isEqualTo(100L);
        assertThat(memberDetails.getUsername()).isEqualTo("100");
        assertThat(memberDetails.isEnabled()).isTrue();
        assertThat(memberDetails.getPassword()).isNull();
        assertThat(memberDetails.getAuthorities()).isEmpty();
    }

    @Test
    void 비활성화된_사용자는_enabled가_false다() {
        Member member = Member.register("홍길동");
        member.deactivate();

        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        assertThat(memberDetails.isEnabled()).isFalse();
    }

    @Test
    void 관리자로_승격된_사용자는_ADMIN_ACCESS_권한을_가진다() {
        Member member = Member.register("홍길동");
        member.grantAdmin();

        CustomMemberDetails memberDetails = new CustomMemberDetails(member);

        assertThat(memberDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN_ACCESS");
    }
}

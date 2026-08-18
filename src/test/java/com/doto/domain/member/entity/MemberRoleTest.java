package com.doto.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MemberRoleTest {

    @Test
    void USER는_아무_권한도_없다() {
        assertThat(MemberRole.USER.getAuthorities()).isEmpty();
    }

    @Test
    void ADMIN은_ADMIN_ACCESS_권한을_가진다() {
        assertThat(MemberRole.ADMIN.getAuthorities()).containsExactly(Authority.ADMIN_ACCESS);
    }

    @Test
    void 권한_목록은_외부에서_변경할_수_없다() {
        assertThatThrownBy(() -> MemberRole.ADMIN.getAuthorities().add(Authority.ADMIN_ACCESS))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}

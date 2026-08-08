package com.doto.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Nested
    class 회원가입 {

        @Test
        void 닉네임으로_가입하면_상태는_ACTIVE이다() {
            Member member = Member.register("홍길동");

            assertThat(member.getNickname()).isEqualTo("홍길동");
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }

        @Test
        void 가입하면_기본_권한은_USER이다() {
            Member member = Member.register("홍길동");

            assertThat(member.getRole()).isEqualTo(MemberRole.USER);
        }
    }

    @Nested
    class 권한_변경 {

        @Test
        void 관리자로_승격할_수_있다() {
            Member member = Member.register("홍길동");

            member.grantAdmin();

            assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
        }

        @Test
        void 관리자_권한을_회수할_수_있다() {
            Member member = Member.register("홍길동");
            member.grantAdmin();

            member.revokeAdmin();

            assertThat(member.getRole()).isEqualTo(MemberRole.USER);
        }
    }

    @Nested
    class 닉네임_수정 {

        @Test
        void 닉네임을_변경할_수_있다() {
            Member member = Member.register("홍길동");

            member.updateNickname("김철수");

            assertThat(member.getNickname()).isEqualTo("김철수");
        }
    }

    @Nested
    class 비활성화 {

        @Test
        void 비활성화하면_상태가_INACTIVE로_바뀐다() {
            Member member = Member.register("홍길동");

            member.deactivate();

            assertThat(member.getStatus()).isEqualTo(MemberStatus.INACTIVE);
        }
    }
}

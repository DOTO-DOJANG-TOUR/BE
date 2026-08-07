package com.doto.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.doto.domain.member.dto.UserResponseDTO;
import com.doto.domain.member.dto.UserUpdateRequestDTO;
import com.doto.domain.member.entity.GeneralAuthAccount;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.domain.member.exception.MemberException;
import com.doto.domain.member.repository.GeneralAuthAccountRepository;
import com.doto.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GeneralAuthAccountRepository generalAuthAccountRepository;

    @InjectMocks
    private MemberService memberService;

    private Member memberWithId(long id) {
        Member member = Member.register("홍길동");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    @Nested
    class 내_정보_조회 {

        @Test
        void 이메일과_함께_사용자_정보를_반환한다() {
            Member member = memberWithId(1L);
            GeneralAuthAccount account = GeneralAuthAccount.create(member, "member@example.com", "encoded");
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(generalAuthAccountRepository.findByMember_Id(1L)).thenReturn(Optional.of(account));

            UserResponseDTO response = memberService.getMyInfo(1L);

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.email()).isEqualTo("member@example.com");
            assertThat(response.nickname()).isEqualTo("홍길동");
        }

        @Test
        void 일반_로그인_계정이_없으면_이메일은_null이다() {
            Member member = memberWithId(1L);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(generalAuthAccountRepository.findByMember_Id(1L)).thenReturn(Optional.empty());

            UserResponseDTO response = memberService.getMyInfo(1L);

            assertThat(response.email()).isNull();
        }

        @Test
        void 존재하지_않는_사용자면_예외를_던진다() {
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.getMyInfo(1L))
                    .isInstanceOf(MemberException.class)
                    .extracting("errorCode")
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    class 내_정보_수정 {

        @Test
        void 닉네임만_보내면_닉네임만_바뀐다() {
            Member member = memberWithId(1L);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            memberService.updateMyInfo(1L, new UserUpdateRequestDTO("김철수"));

            assertThat(member.getNickname()).isEqualTo("김철수");
        }

        @Test
        void 아무_필드도_없으면_아무것도_바뀌지_않는다() {
            Member member = memberWithId(1L);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            memberService.updateMyInfo(1L, new UserUpdateRequestDTO(null));

            assertThat(member.getNickname()).isEqualTo("홍길동");
        }

        @Test
        void 존재하지_않는_사용자면_예외를_던진다() {
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    memberService.updateMyInfo(1L, new UserUpdateRequestDTO("김철수"))
            )
                    .isInstanceOf(MemberException.class)
                    .extracting("errorCode")
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }
}

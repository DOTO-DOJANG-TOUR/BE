package com.doto.domain.member.service;

import com.doto.domain.member.dto.UserResponseDTO;
import com.doto.domain.member.dto.UserUpdateRequestDTO;
import com.doto.domain.member.entity.GeneralAuthAccount;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.entity.SocialAuthAccount;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.domain.member.exception.MemberException;
import com.doto.domain.member.repository.GeneralAuthAccountRepository;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.member.repository.SocialAuthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GeneralAuthAccountRepository generalAuthAccountRepository;
    private final SocialAuthAccountRepository socialAuthAccountRepository;

    @Transactional(readOnly = true)
    public UserResponseDTO getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 회원은 일반(이메일/비밀번호) 계정 또는 소셜 계정 중 하나로 가입돼 있다(현재는 한 회원이
        // 두 방식을 동시에 갖지 않음). general_auth_accounts에 없으면 social_auth_accounts를
        // 확인해서 카카오/구글 로그인 시 저장해 둔 이메일을 대신 사용한다.
        String email = generalAuthAccountRepository.findByMember_Id(memberId)
                .map(GeneralAuthAccount::getEmail)
                .or(() -> socialAuthAccountRepository.findByMember_Id(memberId)
                        .map(SocialAuthAccount::getEmail))
                .orElse(null);

        return UserResponseDTO.from(member, email);
    }

    @Transactional
    public void updateMyInfo(Long memberId, UserUpdateRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (StringUtils.hasText(request.nickname())) {
            member.updateNickname(request.nickname());
        }
    }

}

package com.doto.domain.member.service;

import com.doto.domain.member.dto.MemberResponseDTO;
import com.doto.domain.member.dto.MemberUpdateRequestDTO;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.domain.member.exception.MemberException;
import com.doto.domain.member.repository.GeneralAuthAccountRepository;
import com.doto.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GeneralAuthAccountRepository generalAuthAccountRepository;

    @Transactional(readOnly = true)
    public MemberResponseDTO getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        String email = generalAuthAccountRepository.findByMember_Id(memberId)
                .map(account -> account.getEmail())
                .orElse(null);

        return MemberResponseDTO.from(member, email);
    }

    @Transactional
    public void updateMyInfo(Long memberId, MemberUpdateRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (StringUtils.hasText(request.nickname())) {
            member.updateNickname(request.nickname());
        }
    }

}

package com.doto.domain.member.service;

import com.doto.domain.member.dto.UserResponseDTO;
import com.doto.domain.member.dto.UserUpdateRequestDTO;
import com.doto.domain.member.dto.UserUpdateResponseDTO;
import com.doto.domain.member.entity.GeneralAuthAccount;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.entity.SocialAuthAccount;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.domain.member.exception.MemberException;
import com.doto.domain.member.repository.GeneralAuthAccountRepository;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.domain.member.repository.SocialAuthAccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 30;

    private final MemberRepository memberRepository;
    private final GeneralAuthAccountRepository generalAuthAccountRepository;
    private final SocialAuthAccountRepository socialAuthAccountRepository;

    @Transactional(readOnly = true)
    public UserResponseDTO getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Optional<SocialAuthAccount> socialAccount = socialAuthAccountRepository.findByMember_Id(memberId);

        String email = generalAuthAccountRepository.findByMember_Id(memberId)
                .map(GeneralAuthAccount::getEmail)
                .or(() -> socialAccount.map(SocialAuthAccount::getEmail))
                .orElse(null);

        String provider = socialAccount.map(account -> account.getProvider().name()).orElse(null);
        String profileImg = socialAccount.map(SocialAuthAccount::getProfileImg).orElse(null);

        return UserResponseDTO.from(member, email, provider, profileImg);
    }

    @Transactional
    public UserUpdateResponseDTO updateMyInfo(Long memberId, UserUpdateRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (StringUtils.hasText(request.nickname())) {
            validateNicknameLength(request.nickname());
            member.updateNickname(request.nickname());
        }

        return UserUpdateResponseDTO.from(member);
    }

    private void validateNicknameLength(String nickname) {
        if (nickname.length() < NICKNAME_MIN_LENGTH || nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new MemberException(MemberErrorCode.INVALID_NICKNAME_LENGTH);
        }
    }

}

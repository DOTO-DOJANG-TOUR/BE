package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.GeneralAuthAccount;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.entity.MemberStatus;
import com.doto.domain.member.repository.GeneralAuthAccountRepository;
import com.doto.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SignInService {

    /** 타이밍 공격 방지용 더미 비밀번호 해시
     * 타이밍 공격(timing attack) 은 연산 처리 시간 차이를 관찰해, 비밀 정보(비밀번호, 키, 계정 존재 여부 등)를 추측하는 사이드 채널 공격
     * */

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L2xN/BW5NyZP.5ycd2POKlTa3W5W";

    private final GeneralAuthAccountRepository generalAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthResponseDTO signIn(SignInRequestDTO request) {
        // 일반 계정찾기
        GeneralAuthAccount account = generalAuthAccountRepository.findByEmail(request.email()).orElse(null);

        // 계정이 없어도 더미 해시로 passwordEncoder.matches를 같은 시간에 반환하도록
        String passwordHash = (account != null) ? account.getPasswordHash() : DUMMY_PASSWORD_HASH;
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);
        if (account == null || !passwordMatches) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        // 탈퇴로 휴면 상태였던 계정은 일반 로그인 시 다시 활성화
        Member member = account.getMember();
        boolean reactivated = member.getStatus() != MemberStatus.ACTIVE;
        if (reactivated) {
            member.reactivate();
            log.info("탈퇴 상태였던 회원이 일반 로그인으로 재활성화되었습니다. memberId={}", member.getId());
        }

        //토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = refreshTokenService.issue(member);

        return AuthResponseDTO.of(member, accessToken, refreshToken, reactivated);
    }

}

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignInService {

    /**
     * 계정이 존재하지 않을 때 비교 대상으로 쓰는 더미 해시. 실제 비밀번호로는 절대 일치하지 않는다.
     * 계정 존재 여부와 무관하게 항상 passwordEncoder.matches를 한 번 호출해 두 경로의 소요 시간을
     * 비슷하게 맞춰서, 응답 속도 차이로 가입된 이메일을 추측하는 타이밍 공격을 막기 위한 것이다.
     */
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L2xN/BW5NyZP.5ycd2POKlTa3W5W";

    private final GeneralAuthAccountRepository generalAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthResponseDTO signIn(SignInRequestDTO request) {
        GeneralAuthAccount account = generalAuthAccountRepository.findByEmail(request.email()).orElse(null);

        String passwordHash = (account != null) ? account.getPasswordHash() : DUMMY_PASSWORD_HASH;
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);

        if (account == null || !passwordMatches) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        Member member = account.getMember();
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AuthException(AuthErrorCode.INACTIVE_ACCOUNT);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = refreshTokenService.issue(member);

        return AuthResponseDTO.of(member, accessToken, refreshToken);
    }

}

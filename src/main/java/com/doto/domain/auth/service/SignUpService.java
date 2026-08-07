package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.GeneralAuthAccount;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.GeneralAuthAccountRepository;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignUpService {

    private final MemberRepository memberRepository;
    private final GeneralAuthAccountRepository generalAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthResponseDTO signUp(SignUpRequestDTO request) {
        if (generalAuthAccountRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        Member member = memberRepository.save(Member.register(request.nickname()));

        String passwordHash = passwordEncoder.encode(request.password());
        generalAuthAccountRepository.save(
                GeneralAuthAccount.create(member, request.email(), passwordHash)
        );

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = refreshTokenService.issue(member);

        return AuthResponseDTO.of(member, accessToken, refreshToken);
    }

}

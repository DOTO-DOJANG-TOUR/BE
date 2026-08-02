package com.doto.domain.auth.service;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.user.entity.GeneralAuthAccount;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.repository.GeneralAuthAccountRepository;
import com.doto.domain.user.repository.UserRepository;
import com.doto.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final GeneralAuthAccountRepository generalAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthResponseDTO signUp(SignUpRequestDTO request) {
        if (generalAuthAccountRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        User user = userRepository.save(User.register(request.nickname()));

        String passwordHash = passwordEncoder.encode(request.password());
        // existsByEmail 사전 체크와 실제 저장 사이에 동시에 같은 이메일로 가입 요청이 들어오면
        // 사전 체크만으로는 막을 수 없다. general_auth_accounts.email의 유니크 제약이 최종 방어선이므로,
        // saveAndFlush로 즉시 반영해 제약 위반을 여기서 잡아 DUPLICATE_EMAIL로 변환한다.
        // (실패 시 @Transactional에 의해 앞선 User insert도 함께 롤백된다.)
        try {
            generalAuthAccountRepository.saveAndFlush(
                    GeneralAuthAccount.create(user, request.email(), passwordHash)
            );
        } catch (DataIntegrityViolationException e) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = refreshTokenService.issue(user);

        return AuthResponseDTO.of(user, accessToken, refreshToken);
    }

}

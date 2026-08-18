package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SignUpRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.GeneralAuthAccount;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.GeneralAuthAccountRepository;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GeneralAuthAccountRepository generalAuthAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private SignUpService signUpService;

    @Nested
    class 성공 {

        @Test
        void 이메일이_중복되지_않으면_회원가입에_성공한다() {
            SignUpRequestDTO request = new SignUpRequestDTO("member@example.com", "password1", "홍길동");
            when(generalAuthAccountRepository.existsByEmail("member@example.com")).thenReturn(false);
            when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                ReflectionTestUtils.setField(member, "id", 1L);
                return member;
            });
            when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(any(Member.class))).thenReturn("refresh-token");

            AuthResponseDTO response = signUpService.signUp(request);

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.nickname()).isEqualTo("홍길동");
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            verify(generalAuthAccountRepository).save(any(GeneralAuthAccount.class));
        }
    }

    @Nested
    class 실패 {

        @Test
        void 이메일이_이미_있으면_예외를_던지고_이후_과정은_진행하지_않는다() {
            SignUpRequestDTO request = new SignUpRequestDTO("member@example.com", "password1", "홍길동");
            when(generalAuthAccountRepository.existsByEmail("member@example.com")).thenReturn(true);

            assertThatThrownBy(() -> signUpService.signUp(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.DUPLICATE_EMAIL);

            verifyNoInteractions(memberRepository, jwtTokenProvider, refreshTokenService);
        }
    }
}

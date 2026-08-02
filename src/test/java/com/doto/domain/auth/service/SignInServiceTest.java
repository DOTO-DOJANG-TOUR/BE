package com.doto.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.dto.SignInRequestDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.user.entity.GeneralAuthAccount;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.repository.GeneralAuthAccountRepository;
import com.doto.global.security.jwt.JwtTokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SignInServiceTest {

    @Mock
    private GeneralAuthAccountRepository generalAuthAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private SignInService signInService;

    private User activeUser() {
        User user = User.register("홍길동");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Nested
    class 성공 {

        @Test
        void 이메일과_비밀번호가_맞으면_로그인에_성공한다() {
            User user = activeUser();
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "encoded");
            SignInRequestDTO request = new SignInRequestDTO("user@example.com", "raw-password");

            when(generalAuthAccountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
            when(passwordEncoder.matches("raw-password", "encoded")).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");
            when(refreshTokenService.issue(user)).thenReturn("refresh-token");

            AuthResponseDTO response = signInService.signIn(request);

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
        }
    }

    @Nested
    class 실패 {

        @Test
        void 이메일에_해당하는_계정이_없으면_예외를_던진다() {
            SignInRequestDTO request = new SignInRequestDTO("none@example.com", "raw-password");
            when(generalAuthAccountRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> signInService.signIn(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        void 비밀번호가_틀리면_예외를_던진다() {
            User user = activeUser();
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "encoded");
            SignInRequestDTO request = new SignInRequestDTO("user@example.com", "wrong-password");

            when(generalAuthAccountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
            when(passwordEncoder.matches("wrong-password", "encoded")).thenReturn(false);

            assertThatThrownBy(() -> signInService.signIn(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);

            verifyNoInteractions(jwtTokenProvider, refreshTokenService);
        }

        @Test
        void 비활성화된_계정이면_예외를_던진다() {
            User user = activeUser();
            user.deactivate();
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "encoded");
            SignInRequestDTO request = new SignInRequestDTO("user@example.com", "raw-password");

            when(generalAuthAccountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
            when(passwordEncoder.matches("raw-password", "encoded")).thenReturn(true);

            assertThatThrownBy(() -> signInService.signIn(request))
                    .isInstanceOf(AuthException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INACTIVE_ACCOUNT);

            verifyNoInteractions(jwtTokenProvider, refreshTokenService);
        }
    }
}

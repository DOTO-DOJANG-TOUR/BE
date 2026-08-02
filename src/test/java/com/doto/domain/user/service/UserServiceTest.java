package com.doto.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doto.domain.user.dto.UserResponseDTO;
import com.doto.domain.user.dto.UserUpdateRequestDTO;
import com.doto.domain.user.entity.GeneralAuthAccount;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.exception.UserErrorCode;
import com.doto.domain.user.exception.UserException;
import com.doto.domain.user.repository.GeneralAuthAccountRepository;
import com.doto.domain.user.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeneralAuthAccountRepository generalAuthAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User userWithId(long id) {
        User user = User.register("홍길동");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Nested
    class 내_정보_조회 {

        @Test
        void 이메일과_함께_사용자_정보를_반환한다() {
            User user = userWithId(1L);
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "encoded");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(generalAuthAccountRepository.findByUser_Id(1L)).thenReturn(Optional.of(account));

            UserResponseDTO response = userService.getMyInfo(1L);

            assertThat(response.userId()).isEqualTo("1");
            assertThat(response.email()).isEqualTo("user@example.com");
            assertThat(response.nickname()).isEqualTo("홍길동");
        }

        @Test
        void 일반_로그인_계정이_없으면_이메일은_null이다() {
            User user = userWithId(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(generalAuthAccountRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

            UserResponseDTO response = userService.getMyInfo(1L);

            assertThat(response.email()).isNull();
        }

        @Test
        void 존재하지_않는_사용자면_예외를_던진다() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getMyInfo(1L))
                    .isInstanceOf(UserException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    class 내_정보_수정 {

        @Test
        void 닉네임만_보내면_닉네임만_바뀐다() {
            User user = userWithId(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.updateMyInfo(1L, new UserUpdateRequestDTO("김철수", null, null));

            assertThat(user.getNickname()).isEqualTo("김철수");
            verify(generalAuthAccountRepository, never()).findByUser_Id(1L);
        }

        @Test
        void 아무_필드도_없으면_아무것도_바뀌지_않는다() {
            User user = userWithId(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.updateMyInfo(1L, new UserUpdateRequestDTO(null, null, null));

            assertThat(user.getNickname()).isEqualTo("홍길동");
        }

        @Test
        void 현재_비밀번호가_맞으면_비밀번호가_바뀐다() {
            User user = userWithId(1L);
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "encoded-old");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(generalAuthAccountRepository.findByUser_Id(1L)).thenReturn(Optional.of(account));
            when(passwordEncoder.matches("current-pw", "encoded-old")).thenReturn(true);
            when(passwordEncoder.encode("new-pw")).thenReturn("encoded-new");

            userService.updateMyInfo(1L, new UserUpdateRequestDTO(null, "current-pw", "new-pw"));

            assertThat(account.getPasswordHash()).isEqualTo("encoded-new");
        }

        @Test
        void 현재_비밀번호가_틀리면_예외를_던진다() {
            User user = userWithId(1L);
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "encoded-old");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(generalAuthAccountRepository.findByUser_Id(1L)).thenReturn(Optional.of(account));
            when(passwordEncoder.matches("wrong-pw", "encoded-old")).thenReturn(false);

            assertThatThrownBy(() ->
                    userService.updateMyInfo(1L, new UserUpdateRequestDTO(null, "wrong-pw", "new-pw"))
            )
                    .isInstanceOf(UserException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.INVALID_CURRENT_PASSWORD);

            assertThat(account.getPasswordHash()).isEqualTo("encoded-old");
        }

        @Test
        void 현재_비밀번호_없이_새_비밀번호만_보내면_예외를_던진다() {
            User user = userWithId(1L);
            GeneralAuthAccount account = GeneralAuthAccount.create(user, "user@example.com", "encoded-old");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(generalAuthAccountRepository.findByUser_Id(1L)).thenReturn(Optional.of(account));

            assertThatThrownBy(() ->
                    userService.updateMyInfo(1L, new UserUpdateRequestDTO(null, null, "new-pw"))
            )
                    .isInstanceOf(UserException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.INVALID_CURRENT_PASSWORD);
        }

        @Test
        void 존재하지_않는_사용자면_예외를_던진다() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.updateMyInfo(1L, new UserUpdateRequestDTO("김철수", null, null))
            )
                    .isInstanceOf(UserException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 소셜_로그인_전용_계정이_비밀번호_변경을_시도하면_예외를_던진다() {
            User user = userWithId(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(generalAuthAccountRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.updateMyInfo(1L, new UserUpdateRequestDTO(null, "current-pw", "new-pw"))
            )
                    .isInstanceOf(UserException.class)
                    .extracting("errorCode")
                    .isEqualTo(UserErrorCode.NO_PASSWORD_ACCOUNT);
        }
    }
}

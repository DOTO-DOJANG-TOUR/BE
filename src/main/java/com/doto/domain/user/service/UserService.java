package com.doto.domain.user.service;

import com.doto.domain.user.dto.UserResponseDTO;
import com.doto.domain.user.dto.UserUpdateRequestDTO;
import com.doto.domain.user.entity.GeneralAuthAccount;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.exception.UserErrorCode;
import com.doto.domain.user.exception.UserException;
import com.doto.domain.user.repository.GeneralAuthAccountRepository;
import com.doto.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GeneralAuthAccountRepository generalAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponseDTO getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String email = generalAuthAccountRepository.findByUser_Id(userId)
                .map(account -> account.getEmail())
                .orElse(null);

        return UserResponseDTO.from(user, email);
    }

    @Transactional
    public void updateMyInfo(Long userId, UserUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (StringUtils.hasText(request.nickname())) {
            user.updateNickname(request.nickname());
        }

        if (StringUtils.hasText(request.newPassword())) {
            changePassword(userId, request.currentPassword(), request.newPassword());
        }
    }

    private void changePassword(Long userId, String currentPassword, String newPassword) {
        // 여기 도달했다는 건 updateMyInfo에서 이미 User 존재를 확인했다는 뜻이다.
        // 그런데도 계정이 없다면 "사용자가 없는" 게 아니라 소셜 로그인 전용이라 비밀번호 자체가
        // 없는 계정이라는 뜻이므로, USER_NOT_FOUND와 구분되는 별도 에러 코드로 안내한다.
        GeneralAuthAccount account = generalAuthAccountRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NO_PASSWORD_ACCOUNT));

        if (!StringUtils.hasText(currentPassword)
                || !passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new UserException(UserErrorCode.INVALID_CURRENT_PASSWORD);
        }

        account.changePassword(passwordEncoder.encode(newPassword));
    }

}

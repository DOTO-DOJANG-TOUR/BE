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
        GeneralAuthAccount account = generalAuthAccountRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (!StringUtils.hasText(currentPassword)
                || !passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new UserException(UserErrorCode.INVALID_CURRENT_PASSWORD);
        }

        account.changePassword(passwordEncoder.encode(newPassword));
    }

}

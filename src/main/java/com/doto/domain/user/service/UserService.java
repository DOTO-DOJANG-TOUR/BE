package com.doto.domain.user.service;

import com.doto.domain.user.dto.UserResponseDTO;
import com.doto.domain.user.dto.UserUpdateRequestDTO;
import com.doto.domain.user.entity.User;
import com.doto.domain.user.exception.UserErrorCode;
import com.doto.domain.user.exception.UserException;
import com.doto.domain.user.repository.GeneralAuthAccountRepository;
import com.doto.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GeneralAuthAccountRepository generalAuthAccountRepository;

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
    }

}

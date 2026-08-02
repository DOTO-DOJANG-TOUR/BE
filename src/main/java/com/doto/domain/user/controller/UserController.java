package com.doto.domain.user.controller;

import com.doto.domain.user.dto.UserResponseDTO;
import com.doto.domain.user.dto.UserUpdateRequestDTO;
import com.doto.domain.user.service.UserService;
import com.doto.global.api.CommonResponse;
import com.doto.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<CommonResponse<UserResponseDTO>> getMyInfo(CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                CommonResponse.success(userService.getMyInfo(userDetails.getUserId()))
        );
    }

    @Override
    public ResponseEntity<CommonResponse<UserResponseDTO>> updateMyInfo(
            CustomUserDetails userDetails,
            UserUpdateRequestDTO request
    ) {
        userService.updateNickname(userDetails.getUserId(), request);
        return ResponseEntity.ok(
                CommonResponse.success(userService.getMyInfo(userDetails.getUserId()))
        );
    }

}

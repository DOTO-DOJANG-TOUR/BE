package com.doto.domain.user.controller;

import com.doto.domain.user.dto.UserResponseDTO;
import com.doto.domain.user.dto.UserUpdateRequestDTO;
import com.doto.domain.user.exception.UserErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import com.doto.global.security.CurrentUser;
import com.doto.global.security.CustomUserDetails;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "사용자 API")
@ApiErrorCodeExamples({UserErrorCode.class})
public interface UserApi {

    @Operation(summary = "내 정보 조회")
    @ApiResponse(responseCode = "200", description = "내 정보 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/users/me")
    ResponseEntity<CommonResponse<UserResponseDTO>> getMyInfo(
            @Parameter(hidden = true) @CurrentUser CustomUserDetails userDetails
    );

    @Operation(
            summary = "내 정보 수정",
            description = """
                    보낸 필드만 수정하는 부분 수정(PATCH)입니다.

                    - 닉네임 변경: {"nickname": "홍길동"}
                    - 아무 필드도 보내지 않으면 변경 없이 현재 상태 그대로 200이 반환됩니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "내 정보 수정 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/api/v1/users/me")
    ResponseEntity<CommonResponse<UserResponseDTO>> updateMyInfo(
            @Parameter(hidden = true) @CurrentUser CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequestDTO request
    );

}

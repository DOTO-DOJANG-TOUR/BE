package com.doto.domain.member.controller;

import com.doto.domain.member.dto.UserResponseDTO;
import com.doto.domain.member.dto.UserUpdateRequestDTO;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import com.doto.global.security.CurrentMember;
import com.doto.global.security.CustomMemberDetails;
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

@Tag(name = "Member", description = "사용자 API")
@ApiErrorCodeExamples({MemberErrorCode.class})
public interface MemberApi {

    @Operation(summary = "내 정보 조회")
    @ApiResponse(responseCode = "200", description = "내 정보 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/members/me")
    ResponseEntity<CommonResponse<UserResponseDTO>> getMyInfo(
            @Parameter(hidden = true) @CurrentMember CustomMemberDetails memberDetails
    );

    @Operation(
            summary = "내 정보 수정",
            description = """
                    현재는 닉네임 변경만 되는 것으로 알고 있음
                    """
    )
    @ApiResponse(responseCode = "200", description = "내 정보 수정 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/api/v1/members/me")
    ResponseEntity<CommonResponse<UserResponseDTO>> updateMyInfo(
            @Parameter(hidden = true) @CurrentMember CustomMemberDetails memberDetails,
            @Valid @RequestBody UserUpdateRequestDTO request
    );

}

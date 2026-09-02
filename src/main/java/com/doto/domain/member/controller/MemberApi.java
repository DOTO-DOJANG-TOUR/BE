package com.doto.domain.member.controller;

import com.doto.domain.member.dto.UserResponseDTO;
import com.doto.domain.member.dto.UserUpdateRequestDTO;
import com.doto.domain.member.dto.UserUpdateResponseDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
            @CurrentMember CustomMemberDetails memberDetails
    );

    @Operation(
            summary = "내 정보 수정",
            description = """
                    현재 로그인한 사용자의 닉네임을 변경합니다.
                    - 닉네임은 변경할 때만 입력합니다. null이면 닉네임을 변경하지 않습니다.
                    - 닉네임은 2자 이상 30자 이하여야 합니다.
                    - 추후 profile_img 등의 정보를 변경할 가능성이 있습니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "내 정보 수정 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/api/v1/members/me")
    ResponseEntity<CommonResponse<UserUpdateResponseDTO>> updateMyInfo(
            @CurrentMember CustomMemberDetails memberDetails,
            @Valid @RequestBody UserUpdateRequestDTO request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    현재 로그인한 사용자를 비활성화 처리합니다. 실제 삭제는 14일 후 자동으로 처리됩니다.
                    - 완전 탈퇴 처리(14일)전, 소셜 로그인 계정으로 다시 로그인하면 자동으로 재활성화됩니다.
                    - 성공 응답은 204 No Content이며 body가 없습니다.
                    """
    )
    @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공 (정상 처리, body 없음)")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @DeleteMapping("/api/v1/members/me")
    ResponseEntity<Void> withdraw(
            @Parameter(hidden = true) @CurrentMember CustomMemberDetails memberDetails
    );

}

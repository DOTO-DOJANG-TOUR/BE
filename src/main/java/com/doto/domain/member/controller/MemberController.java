package com.doto.domain.member.controller;

import com.doto.domain.member.dto.UserResponseDTO;
import com.doto.domain.member.dto.UserUpdateRequestDTO;
import com.doto.domain.member.dto.UserUpdateResponseDTO;
import com.doto.domain.member.service.MemberService;
import com.doto.global.api.CommonResponse;
import com.doto.global.security.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @Override
    public ResponseEntity<CommonResponse<UserResponseDTO>> getMyInfo(CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(
                CommonResponse.success(memberService.getMyInfo(memberDetails.getMemberId()))
        );
    }

    @Override
    public ResponseEntity<CommonResponse<UserUpdateResponseDTO>> updateMyInfo(
            CustomMemberDetails memberDetails,
            UserUpdateRequestDTO request
    ) {
        UserUpdateResponseDTO result = memberService.updateMyInfo(memberDetails.getMemberId(), request);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

}

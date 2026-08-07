package com.doto.domain.member.controller;

import com.doto.domain.member.dto.MemberResponseDTO;
import com.doto.domain.member.dto.MemberUpdateRequestDTO;
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
    public ResponseEntity<CommonResponse<MemberResponseDTO>> getMyInfo(CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(
                CommonResponse.success(memberService.getMyInfo(memberDetails.getMemberId()))
        );
    }

    @Override
    public ResponseEntity<CommonResponse<MemberResponseDTO>> updateMyInfo(
            CustomMemberDetails memberDetails,
            MemberUpdateRequestDTO request
    ) {
        memberService.updateMyInfo(memberDetails.getMemberId(), request);
        return ResponseEntity.ok(
                CommonResponse.success(memberService.getMyInfo(memberDetails.getMemberId()))
        );
    }

}

package com.doto.domain.member.dto;

import com.doto.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record MemberResponseDTO(
        @Schema(description = "사용자 ID")
        String memberId,

        @Schema(description = "이메일", example = "member@example.com")
        String email,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "계정 상태", example = "ACTIVE")
        String status,

        @Schema(description = "가입일시")
        Instant createdAt
) {

    public static MemberResponseDTO from(Member member, String email) {
        return new MemberResponseDTO(
                String.valueOf(member.getId()),
                email,
                member.getNickname(),
                member.getStatus().name(),
                member.getCreatedAt()
        );
    }

}

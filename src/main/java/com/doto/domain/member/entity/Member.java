package com.doto.domain.member.entity;

import com.doto.global.common.BaseTimeEntity;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "member_id")
    private Long id;

    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 15)
    private MemberRole role;

    private Member(String nickname, MemberStatus status, MemberRole role) {
        this.nickname = nickname;
        this.status = status;
        this.role = role;
    }

    public static Member register(String nickname) {
        return new Member(nickname, MemberStatus.ACTIVE, MemberRole.USER);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void deactivate() {
        this.status = MemberStatus.INACTIVE;
    }

    public void grantAdmin() {
        this.role = MemberRole.ADMIN;
    }

    public void revokeAdmin() {
        this.role = MemberRole.USER;
    }

}

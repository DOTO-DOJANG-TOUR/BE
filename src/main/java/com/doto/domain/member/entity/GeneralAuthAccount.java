package com.doto.domain.member.entity;

import com.doto.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "general_auth_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralAuthAccount extends BaseTimeEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "email", nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private GeneralAuthAccount(Member member, String email, String passwordHash) {
        this.member = member;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static GeneralAuthAccount create(Member member, String email, String passwordHash) {
        return new GeneralAuthAccount(member, email, passwordHash);
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

}

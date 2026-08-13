package com.doto.domain.member.entity;

import com.doto.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "social_auth_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_social_auth_accounts_provider_external_id",
                columnNames = {"provider", "external_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAuthAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private SocialProvider provider;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "email", length = 190)
    private String email;

    //내부 빌더 지우면 안됨
    @Builder(access = AccessLevel.PRIVATE)
    private SocialAuthAccount(
            Member member,
            SocialProvider provider,
            String issuer,
            String externalId,
            String email
    ) {
        this.member = member;
        this.provider = provider;
        this.issuer = issuer;
        this.externalId = externalId;
        this.email = email;
    }

    public static SocialAuthAccount create(
            Member member,
            SocialProvider provider,
            String issuer,
            String externalId,
            String email
    ) {
        return SocialAuthAccount.builder()
                .member(member)
                .provider(provider)
                .issuer(issuer)
                .externalId(externalId)
                .email(email)
                .build();
    }

}

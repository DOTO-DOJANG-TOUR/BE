package com.doto.domain.user.entity;

import com.doto.global.common.BaseTsidEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTsidEntity {

    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private UserStatus status;

    private User(String nickname, UserStatus status) {
        this.nickname = nickname;
        this.status = status;
    }

    public static User register(String nickname) {
        return new User(nickname, UserStatus.ACTIVE);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

}

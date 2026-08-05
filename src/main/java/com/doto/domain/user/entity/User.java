package com.doto.domain.user.entity;

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
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "user_id")
    private Long id;

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

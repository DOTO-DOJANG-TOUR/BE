package com.doto.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 저장한_사용자를_ID로_다시_조회할_수_있다() {
        User saved = userRepository.save(User.register("홍길동"));

        User found = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getNickname()).isEqualTo("홍길동");
        assertThat(found.getId()).isNotNull();
    }
}

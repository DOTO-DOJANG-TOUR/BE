package com.doto.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.user.entity.RefreshToken;
import com.doto.domain.user.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RefreshTokenRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void 토큰_해시로_조회할_수_있다() {
        User user = userRepository.save(User.register("홍길동"));
        refreshTokenRepository.save(RefreshToken.issue(user, "hashed-token", Instant.now().plusSeconds(60)));

        assertThat(refreshTokenRepository.findByTokenHash("hashed-token")).isPresent();
        assertThat(refreshTokenRepository.findByTokenHash("other-hash")).isEmpty();
    }
}

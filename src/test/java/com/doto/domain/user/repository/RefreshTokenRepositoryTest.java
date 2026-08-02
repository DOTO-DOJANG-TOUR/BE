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

    @Test
    void 사용_가능한_토큰은_원자적으로_폐기되고_같은_토큰을_다시_폐기하면_0행이다() {
        User user = userRepository.save(User.register("홍길동"));
        RefreshToken token = refreshTokenRepository.save(
                RefreshToken.issue(user, "hashed-token", Instant.now().plusSeconds(60))
        );

        int firstAttempt = refreshTokenRepository.revokeIfUsable(token.getId(), Instant.now());
        int secondAttempt = refreshTokenRepository.revokeIfUsable(token.getId(), Instant.now());

        assertThat(firstAttempt).isEqualTo(1);
        assertThat(secondAttempt).isEqualTo(0);
    }

    @Test
    void 만료된_토큰은_폐기_대상에서_제외된다() {
        User user = userRepository.save(User.register("홍길동"));
        RefreshToken token = refreshTokenRepository.save(
                RefreshToken.issue(user, "expired-token", Instant.now().minusSeconds(1))
        );

        int revokedRows = refreshTokenRepository.revokeIfUsable(token.getId(), Instant.now());

        assertThat(revokedRows).isEqualTo(0);
    }
}

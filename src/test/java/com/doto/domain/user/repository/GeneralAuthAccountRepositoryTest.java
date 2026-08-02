package com.doto.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.user.entity.GeneralAuthAccount;
import com.doto.domain.user.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GeneralAuthAccountRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeneralAuthAccountRepository generalAuthAccountRepository;

    private User persistUser() {
        return userRepository.save(User.register("홍길동"));
    }

    @Nested
    class 이메일로_조회 {

        @Test
        void 저장된_이메일로_계정을_찾는다() {
            User user = persistUser();
            generalAuthAccountRepository.save(GeneralAuthAccount.create(user, "user@example.com", "hash"));

            assertThat(generalAuthAccountRepository.findByEmail("user@example.com")).isPresent();
            assertThat(generalAuthAccountRepository.existsByEmail("user@example.com")).isTrue();
        }

        @Test
        void 존재하지_않는_이메일은_없다() {
            assertThat(generalAuthAccountRepository.existsByEmail("nobody@example.com")).isFalse();
            assertThat(generalAuthAccountRepository.findByEmail("nobody@example.com")).isEmpty();
        }
    }

    @Nested
    class 사용자ID로_조회 {

        @Test
        void 사용자_ID로_계정을_찾는다() {
            User user = persistUser();
            generalAuthAccountRepository.save(GeneralAuthAccount.create(user, "user@example.com", "hash"));

            assertThat(generalAuthAccountRepository.findByUser_Id(user.getId())).isPresent();
        }
    }
}

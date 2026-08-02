package com.doto.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.user.entity.SocialAuthAccount;
import com.doto.domain.user.entity.SocialProvider;
import com.doto.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SocialAuthAccountRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocialAuthAccountRepository socialAuthAccountRepository;

    @Test
    void provider와_externalId로_계정을_찾는다() {
        User user = userRepository.save(User.register("홍길동"));
        socialAuthAccountRepository.save(
                SocialAuthAccount.create(user, SocialProvider.KAKAO, "issuer", "external-1", "user@example.com")
        );

        assertThat(socialAuthAccountRepository.findByProviderAndExternalId(SocialProvider.KAKAO, "external-1"))
                .isPresent();
        assertThat(socialAuthAccountRepository.existsByProviderAndExternalId(SocialProvider.KAKAO, "external-1"))
                .isTrue();
        assertThat(socialAuthAccountRepository.existsByProviderAndExternalId(SocialProvider.GOOGLE, "external-1"))
                .isFalse();
    }
}

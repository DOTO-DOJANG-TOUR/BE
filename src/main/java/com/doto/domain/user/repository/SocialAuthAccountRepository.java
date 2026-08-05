package com.doto.domain.user.repository;

import com.doto.domain.user.entity.SocialAuthAccount;
import com.doto.domain.user.entity.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAuthAccountRepository extends JpaRepository<SocialAuthAccount, Long> {

    Optional<SocialAuthAccount> findByProviderAndExternalId(
            SocialProvider provider,
            String externalId
    );

    boolean existsByProviderAndExternalId(
            SocialProvider provider,
            String externalId
    );

}

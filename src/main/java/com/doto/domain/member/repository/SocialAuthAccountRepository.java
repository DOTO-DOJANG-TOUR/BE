package com.doto.domain.member.repository;

import com.doto.domain.member.entity.SocialAuthAccount;
import com.doto.domain.member.entity.SocialProvider;
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

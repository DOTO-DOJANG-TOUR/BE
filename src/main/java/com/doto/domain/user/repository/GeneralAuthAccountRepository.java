package com.doto.domain.user.repository;

import com.doto.domain.user.entity.GeneralAuthAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralAuthAccountRepository extends JpaRepository<GeneralAuthAccount, Long> {

    Optional<GeneralAuthAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<GeneralAuthAccount> findByUser_Id(Long userId);

}

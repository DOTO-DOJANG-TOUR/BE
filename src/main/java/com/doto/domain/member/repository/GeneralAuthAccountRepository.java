package com.doto.domain.member.repository;

import com.doto.domain.member.entity.GeneralAuthAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralAuthAccountRepository extends JpaRepository<GeneralAuthAccount, Long> {

    Optional<GeneralAuthAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<GeneralAuthAccount> findByMember_Id(Long memberId);

}

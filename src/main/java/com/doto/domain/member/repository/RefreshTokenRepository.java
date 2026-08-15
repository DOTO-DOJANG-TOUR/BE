package com.doto.domain.member.repository;

import com.doto.domain.member.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** 사용 가능한 토큰만 원자적으로 폐기한다(중복 사용 방지) */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now "
            + "WHERE r.id = :id AND r.revokedAt IS NULL AND r.expiresAt > :now")
    int revokeIfUsable(@Param("id") Long id, @Param("now") Instant now);

}

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

    /**
     * 사용 가능한(폐기되지 않고 만료되지 않은) 토큰만 원자적으로 폐기한다.
     * 동시에 같은 토큰으로 재발급을 시도해도 이 조건부 UPDATE는 하나의 요청에서만 1행을 갱신하므로,
     * 두 트랜잭션이 동시에 isUsable() 체크를 통과해 토큰을 중복 사용하는 경쟁 조건을 막는다.
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now "
            + "WHERE r.id = :id AND r.revokedAt IS NULL AND r.expiresAt > :now")
    int revokeIfUsable(@Param("id") Long id, @Param("now") Instant now);

}

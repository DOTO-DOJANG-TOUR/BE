package com.doto.domain.member.repository;

import com.doto.global.config.JpaConfig;
import com.doto.global.config.TestcontainersConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/** Repository 테스트가 공유하는 Testcontainers + Flyway 기반 설정 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, TestcontainersConfig.class})
abstract class AbstractRepositoryTest {

}

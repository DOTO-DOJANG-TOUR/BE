package com.doto.domain.member.repository;

import com.doto.global.config.JpaConfig;
import com.doto.global.config.TestcontainersConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/**
 * Repository 테스트가 공유하는 PostgreSQL Testcontainers + Flyway 스키마 기반 설정.
 *
 * <p>{@link TestcontainersConfig}가 관리하는 싱글턴 PostgreSQL 컨테이너를 재사용한다. 컨테이너를 왜
 * 직접 관리하지 않고 이 설정을 import하는지는 {@link TestcontainersConfig}의 문서를 참고한다.
 *
 * <p>{@code @DataJpaTest}는 슬라이스 테스트라 애플리케이션의 일반 {@code @Configuration} 빈을
 * 자동으로 스캔하지 않는다. {@code @EnableJpaAuditing}이 선언된 {@link JpaConfig}를 명시적으로
 * import하지 않으면 {@code @CreatedDate}/{@code @LastModifiedDate}가 채워지지 않아
 * created_at/updated_at NOT NULL 제약을 위반한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, TestcontainersConfig.class})
abstract class AbstractRepositoryTest {

}

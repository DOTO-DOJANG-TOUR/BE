package com.doto.domain.user.repository;

import com.doto.global.config.JpaConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Repository 테스트가 공유하는 PostgreSQL Testcontainers + Flyway 스키마 기반 설정.
 *
 * <p>컨테이너를 "싱글턴"으로 한 번만 띄워 모든 하위 테스트 클래스가 같은 포트를 재사용하게 한다.
 * {@code @Container}로 클래스 단위 시작/종료를 맡기면, 클래스마다 컨테이너가 재시작되면서 포트가
 * 바뀌는데, Spring이 이전 클래스에서 캐시해 둔 ApplicationContext(옛 포트로 연결된 DataSource)를
 * 재사용해버려 연결 실패가 난다. 그래서 static 블록에서 직접 한 번만 start()하고 멈추지 않는다
 * (JVM 종료 시 Testcontainers Ryuk이 정리한다).
 *
 * <p>{@code @DataJpaTest}는 슬라이스 테스트라 애플리케이션의 일반 {@code @Configuration} 빈을
 * 자동으로 스캔하지 않는다. {@code @EnableJpaAuditing}이 선언된 {@link JpaConfig}를 명시적으로
 * import하지 않으면 {@code @CreatedDate}/{@code @LastModifiedDate}가 채워지지 않아
 * created_at/updated_at NOT NULL 제약을 위반한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
abstract class AbstractRepositoryTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:17-alpine");

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName("doto_test")
            .withUsername("doto")
            .withPassword("doto");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

}

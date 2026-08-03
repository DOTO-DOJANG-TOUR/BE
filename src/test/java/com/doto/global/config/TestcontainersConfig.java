package com.doto.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트 전역에서 재사용하는 PostgreSQL Testcontainers 싱글턴 설정.
 *
 * <p>컨테이너를 static 필드로 한 번만 만들고, {@code @ServiceConnection}이 붙은 Bean 메서드가
 * 항상 같은 인스턴스를 반환하게 해서, 이 설정을 import하는 모든 테스트 클래스가 같은 컨테이너(같은 포트)를
 * 공유한다.
 *
 * <p>{@code @Container} + {@code @Testcontainers}로 클래스 단위 생명주기를 맡기면 테스트 클래스마다
 * 컨테이너가 새로 시작되며 포트가 바뀌는데, Spring이 이전 클래스에서 캐시해 둔 ApplicationContext(옛 포트로
 * 연결된 DataSource)를 재사용해버려 연결 실패가 난다. 컨테이너를 Spring Bean으로 등록해두면 Spring이
 * "다른 Bean보다 먼저 시작하고 나중에 정지"시켜주므로 별도 시작/정지 로직이 필요 없고, 여러 컨텍스트가 같은
 * static 인스턴스를 공유해도 안전하다(Testcontainers는 이미 시작된 컨테이너에 대한 재시작 호출을 무시한다).
 * JVM 종료 시에는 Testcontainers Ryuk이 정리한다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                    .withDatabaseName("doto_test")
                    .withUsername("doto")
                    .withPassword("doto");

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return POSTGRES;
    }

}

package com.doto.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** 테스트 전역에서 재사용하는 PostgreSQL Testcontainers 싱글톤 설정 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    DockerImageName.parse("postgis/postgis:17-3.5-alpine")
                            .asCompatibleSubstituteFor("postgres")
            )
                    .withDatabaseName("doto_test")
                    .withUsername("doto")
                    .withPassword("doto");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }

}

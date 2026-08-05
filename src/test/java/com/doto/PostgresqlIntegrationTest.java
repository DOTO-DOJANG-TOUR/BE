package com.doto;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.global.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Import(TestcontainersConfig.class)
class PostgresqlIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void PostgreSQL에_연결해_운영과_동일한_DB_환경을_검증한다() {
        String databaseName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
        String databaseVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);

        assertThat(databaseName).isEqualTo("doto_test");
        assertThat(databaseVersion).contains("PostgreSQL 17");
    }
}

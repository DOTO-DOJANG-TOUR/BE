package com.doto.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotenvLoaderTest {

    private static final String TEST_KEY = "DOTENV_LOADER_TEST_KEY";

    @TempDir
    private Path tempDir;

    @AfterEach
    void tearDown() {
        System.clearProperty(TEST_KEY);
    }

    @Test
    void 존재하지_않는_파일이면_아무_일도_일어나지_않는다() {
        DotenvLoader.load(tempDir.resolve("no-such-file.env"));

        assertThat(System.getProperty(TEST_KEY)).isNull();
    }

    @Test
    void KEY_VALUE_라인을_System_property로_등록한다() throws IOException {
        Path envFile = writeEnvFile(TEST_KEY + "=hello-world");

        DotenvLoader.load(envFile);

        assertThat(System.getProperty(TEST_KEY)).isEqualTo("hello-world");
    }

    @Test
    void 따옴표로_감싼_값은_따옴표를_제거하고_등록한다() throws IOException {
        Path envFile = writeEnvFile(TEST_KEY + "=\"hello world\"");

        DotenvLoader.load(envFile);

        assertThat(System.getProperty(TEST_KEY)).isEqualTo("hello world");
    }

    @Test
    void 빈_줄과_주석은_무시한다() throws IOException {
        Path envFile = writeEnvFile("", "# comment", TEST_KEY + "=value");

        DotenvLoader.load(envFile);

        assertThat(System.getProperty(TEST_KEY)).isEqualTo("value");
    }

    @Test
    void 이미_설정된_System_property가_있으면_env_파일_값으로_덮어쓰지_않는다() throws IOException {
        System.setProperty(TEST_KEY, "already-set");
        Path envFile = writeEnvFile(TEST_KEY + "=from-env-file");

        DotenvLoader.load(envFile);

        assertThat(System.getProperty(TEST_KEY)).isEqualTo("already-set");
    }

    private Path writeEnvFile(String... lines) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, List.of(lines));
        return envFile;
    }
}

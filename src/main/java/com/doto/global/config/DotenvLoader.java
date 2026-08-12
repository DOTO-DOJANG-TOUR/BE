package com.doto.global.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 로컬 개발 편의를 위해 프로젝트 루트의 {@code .env} 파일을 읽어 System property로 등록한다.
 *
 * <p>Spring Boot는 {@code .env} 파일을 자동으로 읽지 않으므로, {@code application.yml}의
 * {@code ${KAKAO_OIDC_CLIENT_ID}} 같은 플레이스홀더를 채우려면 실제 환경변수를 넣어주거나
 * 이 로더로 System property를 채워야 한다.
 *
 * <p>이미 설정된 실제 OS 환경변수나 System property가 있으면 그 값을 그대로 쓰고
 * {@code .env} 값으로 덮어쓰지 않는다 — 배포 환경에서는 이 파일이 없어도 문제없고,
 * 있어도 실제 환경변수가 항상 우선한다.
 */
public final class DotenvLoader {

    private static final Logger log = LoggerFactory.getLogger(DotenvLoader.class);

    private DotenvLoader() {
    }

    public static void load() {
        load(Path.of(".env"));
    }

    static void load(Path envFile) {
        if (!Files.isRegularFile(envFile)) {
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn(".env 파일을 읽지 못했습니다: {}", e.getMessage());
            return;
        }

        int loadedCount = 0;
        for (String line : lines) {
            if (applyLine(line)) {
                loadedCount++;
            }
        }

        if (loadedCount > 0) {
            log.info(".env에서 {}개의 값을 로컬 System property로 불러왔습니다.", loadedCount);
        }
    }

    private static boolean applyLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return false;
        }

        int separatorIndex = trimmed.indexOf('=');
        if (separatorIndex <= 0) {
            return false;
        }

        String key = trimmed.substring(0, separatorIndex).trim();
        String value = unquote(trimmed.substring(separatorIndex + 1).trim());

        // 실제 환경변수나 이미 지정된 System property(예: -D 옵션)가 있으면 .env보다 우선한다.
        if (System.getenv(key) != null || System.getProperty(key) != null) {
            return false;
        }

        System.setProperty(key, value);
        return true;
    }

    private static String unquote(String value) {
        boolean wrapped = value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")));
        return wrapped ? value.substring(1, value.length() - 1) : value;
    }

}

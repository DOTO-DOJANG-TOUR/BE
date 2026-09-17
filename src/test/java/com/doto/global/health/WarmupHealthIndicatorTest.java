package com.doto.global.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

@DisplayName("WarmupHealthIndicator 단위 테스트")
class WarmupHealthIndicatorTest {

    @Test
    @DisplayName("워밍업 완료 전에는 DOWN을 반환한다")
    void returnsDownBeforeWarmup() {
        WarmupHealthIndicator indicator = new WarmupHealthIndicator();

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("워밍업 완료 후에는 UP을 반환한다")
    void returnsUpAfterWarmup() {
        WarmupHealthIndicator indicator = new WarmupHealthIndicator();

        indicator.markWarmedUp();

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }
}

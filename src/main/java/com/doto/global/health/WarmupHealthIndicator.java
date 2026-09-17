package com.doto.global.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

// 재시작 직후 콜드 스타트 상태에서 실제 트래픽을 받지 않도록
// 워밍업이 끝나기 전까지 /actuator/health를 DOWN으로 응답해 docker-compose healthcheck를 통과하지 못하게 함
@Component
public class WarmupHealthIndicator implements HealthIndicator {

    private volatile boolean warmedUp = false;

    @Override
    public Health health() {
        return warmedUp ? Health.up().build() : Health.down().withDetail("reason", "warming up").build();
    }

    void markWarmedUp() {
        this.warmedUp = true;
    }
}

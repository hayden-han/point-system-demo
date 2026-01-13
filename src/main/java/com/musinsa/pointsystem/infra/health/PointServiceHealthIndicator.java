package com.musinsa.pointsystem.infra.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 포인트 서비스 전체 헬스체크
 * - Redis + Database 통합 상태 확인
 * - 서비스 가용성 판단
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointServiceHealthIndicator implements HealthIndicator {

    private final RedisHealthIndicator redisHealthIndicator;
    private final DatabaseHealthIndicator databaseHealthIndicator;

    @Override
    public Health health() {
        Health redisHealth = redisHealthIndicator.health();
        Health dbHealth = databaseHealthIndicator.health();

        boolean redisUp = redisHealth.getStatus().equals(org.springframework.boot.actuate.health.Status.UP);
        boolean dbUp = dbHealth.getStatus().equals(org.springframework.boot.actuate.health.Status.UP);

        if (redisUp && dbUp) {
            return Health.up()
                    .withDetail("redis", redisHealth.getDetails())
                    .withDetail("database", dbHealth.getDetails())
                    .build();
        }

        // 하나라도 down이면 서비스 불가
        return Health.down()
                .withDetail("redis", redisHealth.getDetails())
                .withDetail("database", dbHealth.getDetails())
                .withDetail("message", buildFailureMessage(redisUp, dbUp))
                .build();
    }

    private String buildFailureMessage(boolean redisUp, boolean dbUp) {
        if (!redisUp && !dbUp) {
            return "Redis와 Database 모두 연결 실패";
        }
        if (!redisUp) {
            return "Redis 연결 실패 - 분산락 불가";
        }
        return "Database 연결 실패 - 데이터 처리 불가";
    }
}

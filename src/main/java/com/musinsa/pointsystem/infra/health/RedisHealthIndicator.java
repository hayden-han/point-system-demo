package com.musinsa.pointsystem.infra.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Redis 헬스체크
 * - Redisson 클라이언트 연결 상태 확인
 * - 간단한 PING 명령으로 응답 확인
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedissonClient redissonClient;

    @Override
    public Health health() {
        try {
            // Redis PING 명령 실행
            long startTime = System.currentTimeMillis();
            redissonClient.getKeys().count();  // 간단한 명령으로 연결 확인
            long responseTime = System.currentTimeMillis() - startTime;

            if (responseTime > 1000) {
                // 응답 시간이 1초 초과면 경고
                return Health.up()
                        .withDetail("status", "slow")
                        .withDetail("responseTimeMs", responseTime)
                        .build();
            }

            return Health.up()
                    .withDetail("status", "connected")
                    .withDetail("responseTimeMs", responseTime)
                    .build();

        } catch (Exception e) {
            log.error("Redis 헬스체크 실패", e);
            return Health.down()
                    .withDetail("status", "disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

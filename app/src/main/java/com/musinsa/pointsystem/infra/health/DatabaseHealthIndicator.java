package com.musinsa.pointsystem.infra.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database 헬스체크
 * - Primary/Replica 연결 상태 확인
 * - 간단한 SELECT 쿼리로 응답 확인
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            // 간단한 쿼리 실행으로 연결 확인
            long startTime = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;

            if (responseTime > 500) {
                // 응답 시간이 500ms 초과면 경고
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
            log.error("Database 헬스체크 실패", e);
            return Health.down()
                    .withDetail("status", "disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

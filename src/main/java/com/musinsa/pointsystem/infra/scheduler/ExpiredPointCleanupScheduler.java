package com.musinsa.pointsystem.infra.scheduler;

import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.infra.persistence.repository.PointLedgerJpaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 만료된 포인트 정리 스케줄러
 * - 매일 새벽 3시에 실행
 * - 만료된 적립건의 잔액을 0으로 설정
 */
@Component
@Slf4j
public class ExpiredPointCleanupScheduler {

    private final PointLedgerJpaRepository pointLedgerJpaRepository;
    private final TimeProvider timeProvider;
    private final Counter cleanupCounter;

    public ExpiredPointCleanupScheduler(PointLedgerJpaRepository pointLedgerJpaRepository,
                                         TimeProvider timeProvider,
                                         MeterRegistry meterRegistry) {
        this.pointLedgerJpaRepository = pointLedgerJpaRepository;
        this.timeProvider = timeProvider;
        this.cleanupCounter = Counter.builder("point.expired.cleanup")
                .description("Number of expired point ledgers cleaned up")
                .register(meterRegistry);
    }

    /**
     * 매일 새벽 3시에 만료된 포인트 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredPoints() {
        log.info("만료 포인트 정리 시작");
        LocalDateTime now = timeProvider.now();

        try {
            int updatedCount = pointLedgerJpaRepository.markExpiredLedgersAsZeroBalance(now);
            cleanupCounter.increment(updatedCount);
            log.info("만료 포인트 정리 완료. 처리 건수: {}", updatedCount);
        } catch (Exception e) {
            log.error("만료 포인트 정리 중 오류 발생", e);
        }
    }
}
